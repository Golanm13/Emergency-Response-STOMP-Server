#pragma once

#include "FrameEncoderDecoder.h"
#include "event.h"
#include "ConnectionHandler.h"
#include <string>
#include <iostream>
#include <map>
#include <vector>
using namespace std;

// TODO: implement the STOMP protocol
class StompProtocol
{
private:
    int receiptId;
    int subscriptionId;
    map<int, string> pendingRecieptsToStdout;
    map<int, string> pendingRecieptsToCommand;
    map<string, vector<Event>> topicToEvent;
    map<string, int> topicToSubscriptionId;
    int waitingToBeReported;
    bool terminate;
    std::atomic<bool> &isLoggedIn;
    std::condition_variable &cv;
    std::mutex &mtx;

public:
    StompProtocol(std::atomic<bool> &isLoggedIn, std::condition_variable &cv, std::mutex &mtx);
    bool shouldTerminate();
    bool processMessage(string &message);
    string processFrame(Frame frame, string response);
    void terminateProtocol();
    void unsubscribe(string &topic);
    map<string, vector<Event>> getTopicToEvent();
    bool isSubscribed(string &topic);
};
