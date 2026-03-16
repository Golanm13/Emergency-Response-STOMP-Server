#include "../include/keyboardInputHandler.h"
#include <fstream>
#include <ctime>

KeyboardInputHandler::KeyboardInputHandler(StompProtocol &stompProtocol, std::atomic<bool> &isLoggedIn, std::condition_variable &cv, std::mutex &mtx)
    : inputThread(), stompProtocol(stompProtocol), connectionHandler(), isLoggedIn(isLoggedIn), cv(cv), mtx(mtx), currentUser("") {};

void KeyboardInputHandler::start()
{
    handleInput();
}

void KeyboardInputHandler::stop()
{
}

void KeyboardInputHandler::handleInput()
{
    while (1)
    {
        std::string input = "";
        std::getline(std::cin, input);
        std::istringstream iss(input);
        string command;
        iss >> command;
        createFrame(command, iss);
    }
}

void KeyboardInputHandler::initServerListener()
{
    auto serverListener = std::make_shared<ServerListener>();
    thread serverListenerThread(
        &ServerListener::listen, serverListener, std::ref(*connectionHandler), std::ref(stompProtocol));
    serverListenerThread.detach();
}

void KeyboardInputHandler::createFrame(const std::string &command, istringstream &inputStream)
{
    std::map<std::string, std::string> headers;
    if (command == "login")
    {
        if (!isLoggedIn)
        {
            std::vector<std::string> tokens;
            string address;
            inputStream >> address;
            split_str(address, ':', tokens);
            if (tokens.size() != 2)
            {
                std::cerr << "Invalid {host:port}" << std::endl;
                return;
            }
            string user, pass;
            inputStream >> user >> pass;
            if (user == "" || pass == "")
            {
                std::cerr << "login command needs 3 args {host:port} {username} {password}" << std::endl;
                return;
            }
            string anotherArg;
            inputStream >> anotherArg;
            if (anotherArg != "")
            {
                std::cerr << "login command needs 3 args {host:port} {username} {password}" << std::endl;
                return;
            }
            connectionHandler = new ConnectionHandler(tokens.at(0), stoi(tokens.at(1)));
            if (connectionHandler->connect())
            {
                // initServerListener();
                cout << "Connected to server" << endl;
                std::map<std::string, std::string> headers;
                headers["login"] = user;
                headers["passcode"] = pass;
                // i need to send the frame to the server line by line this method returns the frame as a string
                string frameString = stompProtocol.processFrame(Frame("CONNECT", headers, ""), "Connected to server");
                // send the frame to the server
                if (!connectionHandler->sendLine(frameString))
                {
                    std::cerr << "Failed to send frame" << std::endl;
                }
                string response;
                if (!connectionHandler->getLine(response))
                {
                    std::cerr << "Failed to get frame" << std::endl;
                }
                bool isProccessed = stompProtocol.processMessage(response);
                if (isProccessed == false)
                {
                    cout << "Login failed" << endl;
                    return;
                }
                std::unique_lock<std::mutex> lock(mtx);
                cv.wait(lock, [&]
                        { return isLoggedIn.load(); });
                currentUser = user;
                initServerListener();
            }
            else
            {
                std::cerr << "Failed to login, try again" << std::endl;
            }
        }
        else
        {
            std::cerr << "Already logged in" << std::endl;
        }
    }
    else if (isLoggedIn)
    {
        if (command == "logout")
        {
            string frameString = stompProtocol.processFrame(Frame("DISCONNECT", headers, ""), "Logged out");
            if (!connectionHandler->sendLine(frameString))
            {
                std::cerr << "Failed to send frame" << std::endl;
            }
            isLoggedIn.store(false);
        }
        else if (command == "join")
        {
            string topic;
            inputStream >> topic;
            headers["destination"] = topic;
            if (!connectionHandler->sendFrameAscii(stompProtocol.processFrame(Frame("SUBSCRIBE", headers, ""), "Joined channel " + topic), '\0'))
            {
                std::cerr << "Failed to send frame" << std::endl;
            }
        }
        else if (command == "report")
        {
            string fileName;
            inputStream >> fileName;
            names_and_events fileData = parseEventsFile(fileName);
            vector<Event> events = fileData.events;
            for (Event event : events)
            {
                if (stompProtocol.shouldTerminate())
                {
                    cout << "Login failed" << endl;
                    return;
                }
                event.setEventOwnerUser(currentUser);
                headers["destination"] = fileData.channel_name;
                string frameString = stompProtocol.processFrame(Frame("SEND", headers, event.toString()), "");
                if (!connectionHandler->sendFrameAscii(frameString, '\0'))
                {
                    std::cerr << "Failed to send frame" << std::endl;
                }
            }
        }
        else if (command == "summary")
        {
            string channelName;
            inputStream >> channelName;
            if (channelName == "")
            {
                std::cerr << "summary command needs 3 args: {channel_name} {username} {file}" << std::endl;
                return;
            }
            if (!stompProtocol.isSubscribed(channelName))
            {
                std::cerr << "Not subscribed to channel" << std::endl;
                return;
            }
            string userName;
            inputStream >> userName;
            if (userName == "")
            {
                std::cerr << "Invalid user name" << std::endl;
                return;
            }
            string fileName;
            inputStream >> fileName;
            if (fileName == "")
            {
                std::cerr << "Invalid file name" << std::endl;
                return;
            }
            // for every event in topicToEvent write it to the file
            ofstream file(fileName);
            // print all events in stompProtocol
            map<string, vector<Event>> topicToEvent = stompProtocol.getTopicToEvent();
            string summaryHeader = "Channel " + channelName + "\n" + "Stats: \n";
            string summaryBody = "";
            int sumOfEvents = 0;
            int sumOfActiveEvents = 0;
            int sumOfForcesArrival = 0;
            auto &topic = topicToEvent[channelName];

            sort(topic.begin(), topic.end(), [](Event &a, Event &b)
                 { return a.get_date_time() < b.get_date_time(); });
            for (Event event : topic)
            {
                if (event.getEventOwnerUser() == userName)
                {
                    sumOfEvents++;
                    auto general_information = event.get_general_information();
                    if (general_information["active"] == "true")
                    {
                        sumOfActiveEvents++;
                    }
                    if (general_information["forces_arrival_at_scene"] == "true")
                    {
                        sumOfForcesArrival++;
                    }
                    summaryBody += "Report_" + to_string(sumOfEvents) + ":\n" + "\tcity: " + event.get_city() + "\n" + "\tdate time: " + epochToDate(event.get_date_time()) + "\n" + "\tevent name: " + event.get_name() + "\n" + "\tsummary: " + event.get_description().substr(0, 27) + "...\n";
                }
            }
            summaryHeader += "Total: " + to_string(sumOfEvents) + "\n" + "active: " + to_string(sumOfActiveEvents) + "\n" + "forces arrival at scene: " + to_string(sumOfForcesArrival) + "\n" + "\nEvent Reports:" + "\n\n";
            file << summaryHeader + summaryBody;
        }
        else if (command == "exit")
        {
            string topic;
            inputStream >> topic;
            if (!stompProtocol.isSubscribed(topic))
            {
                std::cerr << "Not subscribed to channel" << std::endl;
                return;
            }
            headers["destination"] = topic;

            if (!connectionHandler->sendFrameAscii(stompProtocol.processFrame(Frame("UNSUBSCRIBE", headers, ""), "Exited channel " + topic), '\0'))
            {
                std::cerr << "Failed to send frame" << std::endl;
            }
        }
        else
        {
            std::cerr << "Invalid command" << std::endl;
        }
    }
    else
    {
        std::cout << "Not logged in" << std::endl;
    }
}
std::string KeyboardInputHandler::epochToDate(time_t epochTime)
{
    std::tm *tm = std::localtime(&epochTime);
    char buffer[80];
    strftime(buffer, sizeof(buffer), "%Y-%m-%d %H:%M:%S", tm);
    return std::string(buffer);
}

void KeyboardInputHandler::join()
{
    if (inputThread.joinable())
    {
        inputThread.join();
    }
}