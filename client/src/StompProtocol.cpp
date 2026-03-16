#include "../include/StompProtocol.h"

StompProtocol::StompProtocol(std::atomic<bool> &isLoggedIn, std::condition_variable &cv, std::mutex &mtx) : receiptId(1), subscriptionId(1), pendingRecieptsToStdout(), pendingRecieptsToCommand(), topicToEvent(), topicToSubscriptionId(), waitingToBeReported(0), terminate(false), isLoggedIn(isLoggedIn), cv(cv), mtx(mtx) {}

map<string, vector<Event>> StompProtocol::getTopicToEvent()
{
    return topicToEvent;
}

bool StompProtocol::processMessage(string &message)
{
    Frame frame(message);
    // cout << message << endl;
    string command = frame.getCommand();
    if (command == "RECEIPT")
    {
        // cout << "Receipt: " << frame.getHeaders().at("receipt-id") << endl;
        int receiptId = stoi(frame.getHeaders().at("receipt-id"));
        if (pendingRecieptsToCommand[receiptId] != "SEND")
            cout << pendingRecieptsToStdout[receiptId] << endl;
        if (pendingRecieptsToCommand[receiptId] == "SEND")
        {
            waitingToBeReported--;
            if (waitingToBeReported == 0)
            {
                cout << "reported" << endl;
            }
        }
        pendingRecieptsToStdout.erase(receiptId);
        if (pendingRecieptsToCommand[receiptId] == "DISCONNECT")
        {
            terminate = true;
        }
        pendingRecieptsToCommand.erase(receiptId);
    }
    else if (command == "CONNECTED")
    {
        isLoggedIn.store(true);
        cv.notify_all();
        cout << "Login successful" << endl;
    }
    else if (command == "MESSAGE")
    {
        // cout << "Message has been received" << endl;
        string topic = frame.getHeaders().at("destination");
        Event event(frame.getBody());
        topicToEvent[topic].push_back(event);
    }
    else if (command == "ERROR")
    {
        // cout << "Error!" << endl;
        cout << "Error: " << frame.getHeaders().at("message") << endl;
        waitingToBeReported = 0;
        terminate = true;
        isLoggedIn.store(false);
        return false;
    }
    return true;
}

void StompProtocol::terminateProtocol()
{
    terminate = true;
    isLoggedIn.store(false);
}

void StompProtocol::unsubscribe(string &topic)
{
    if (topicToSubscriptionId.find(topic) != topicToSubscriptionId.end())
    {
        topicToSubscriptionId.erase(topic);
    }
}

string StompProtocol::processFrame(Frame frame, string response)
{
    string command = frame.getCommand();
    if (command == "SUBSCRIBE")
    {
        string topic = frame.getHeaders().at("destination");
        topicToSubscriptionId[topic] = subscriptionId++;
        frame.addHeader("id", to_string(topicToSubscriptionId[topic]));
    }
    if (command == "UNSUBSCRIBE")
    {
        string topic = frame.getHeaders().at("destination");
        frame.addHeader("id", to_string(topicToSubscriptionId[topic]));
        unsubscribe(topic);
    }
    if (command != "CONNECT")
    {
        pendingRecieptsToStdout[receiptId] = response;
        pendingRecieptsToCommand[receiptId] = command;
        frame.addHeader("receipt", to_string(receiptId++));
    }
    if (command == "CONNECT")
    {
        terminate = false;
        frame.addHeader("accept-version", "1.2");
        frame.addHeader("host", "stomp.cs.bgu.ac.il");
    }
    if (command == "SEND")
    {
        waitingToBeReported++;
    }
    if (command == "DISCONNECT")
    {
        topicToSubscriptionId.clear();
    }
    return frame.toString();
}

bool StompProtocol::shouldTerminate()
{
    return terminate;
}

bool StompProtocol::isSubscribed(string &topic)
{
    return topicToSubscriptionId.find(topic) != topicToSubscriptionId.end();
}
