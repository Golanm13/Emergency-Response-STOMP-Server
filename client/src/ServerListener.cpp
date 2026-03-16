#include "../include/ServerListener.h"

ServerListener::ServerListener()
    : running(false) {}
ServerListener::~ServerListener()
{
    running.store(false);
}

void ServerListener::listen(ConnectionHandler &connectionHandler, StompProtocol &stompProtocol)
{
    running.store(true);
    while (running)
    {
        std::string response;
        if (connectionHandler.getLine(response))
        {
            stompProtocol.processMessage(response);
        }
        else
        {
            running.store(false);
            connectionHandler.close();
            stompProtocol.terminateProtocol();
        }

        if (stompProtocol.shouldTerminate())
        {
            running.store(false);
            connectionHandler.close();
        }
    }
}
