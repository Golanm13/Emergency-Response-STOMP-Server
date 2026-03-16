#pragma once

#include "utils.h"
#include <string>
#include <iostream>
#include <map>

class Frame
{
private:
    std::string command;
    std::map<std::string, std::string> headers;
    std::string body;

public:
    std::string getCommand();
    std::string extractErrorMessage();
    std::map<std::string, std::string> getHeaders();
    std::string getBody();
    void addHeader(std::string key, std::string value);
    std::string toString();
    Frame(std::string command, std::map<std::string, std::string> headers, std::string body);
    Frame(std::string frameString);
};
