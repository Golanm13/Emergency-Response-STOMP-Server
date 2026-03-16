#pragma once

#include <thread>
#include <atomic>
#include "ConnectionHandler.h"
#include "StompProtocol.h"

class ServerListener
{
public:
    ServerListener();
    ~ServerListener();
    void start();
    void stop();
    void listen(ConnectionHandler &ConnectionHandler, StompProtocol &stompProtocol);
    void join();

private:
    std::atomic<bool> running;
    void readFrame();
};
