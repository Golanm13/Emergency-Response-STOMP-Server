#pragma once

#include <thread>
#include <atomic>
#include "ConnectionHandler.h"
#include "StompProtocol.h"
#include "ServerListener.h"

class KeyboardInputHandler
{
public:
    KeyboardInputHandler(StompProtocol &stompProtocol, std::atomic<bool> &isLoggedIn, std::condition_variable &cv, std::mutex &mtx);
    ~KeyboardInputHandler();
    KeyboardInputHandler(const KeyboardInputHandler &) = delete;
    KeyboardInputHandler &operator=(const KeyboardInputHandler &) = delete;
    void start();
    void stop();
    void handleInput();
    void join();

private:
    std::thread inputThread;
    StompProtocol &stompProtocol;
    ConnectionHandler *connectionHandler;
    std::atomic<bool> &isLoggedIn;
    std::condition_variable &cv;
    std::mutex &mtx;
    string currentUser;
    void createFrame(const std::string &input, std::istringstream &iss);
    void initServerListener();
    string epochToDate(time_t epochTime);
};
