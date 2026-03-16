#include "../include/FrameEncoderDecoder.h"

Frame::Frame(std::string command, std::map<std::string, std::string> headers, std::string body) : command(), headers(), body()
{
    this->command = command;
    this->headers = headers;
    this->body = body;
}
// end of message is \0
std::string Frame::toString()
{
    std::string frameString = command + "\n";
    for (auto &header : headers)
    {
        frameString += header.first + ":" + header.second + "\n";
    }
    frameString += "\n";
    if (body != "")
    {
        frameString += body;
    }
    frameString += "\0";
    return frameString;
}
Frame::Frame(std::string frameString) : command(), headers(), body()
{
    std::istringstream iss(frameString);
    std::string line;
    std::getline(iss, command, '\n');
    while (std::getline(iss, line, '\n'))
    {
        if (line == "")
        {
            break;
        }
        std::vector<std::string> tokens;
        split_str(line, ':', tokens);
        headers[tokens.at(0)] = tokens.at(1);
    }
    while (std::getline(iss, line, '\n'))
    {
        body += line + "\n";
    }
    body = body.substr(0, body.size() - 1);
}

std::string Frame::extractErrorMessage()
{
    if (command != "ERROR")
    {
        return "";
    }
    int endOfmessage = 1;
    std::string errorMessage;
    std::string line;
    std::istringstream iss(body);
    while (std::getline(iss, line, '\n'))
    {
        if (line.find("-----") != std::string::npos)
        {
            if (endOfmessage == 0)
            {
                while (std::getline(iss, line, '\n'))
                {
                    errorMessage += line + "\n";
                }
                return errorMessage;
            }
            endOfmessage--;
        }
        if (endOfmessage == 0)
        {
            errorMessage += line + "\n";
        }
    }
    return "";
}

std::string Frame::getCommand()
{
    return command;
}

std::map<std::string, std::string> Frame::getHeaders()
{
    return headers;
}

void Frame::addHeader(std::string key, std::string value)
{
    headers[key] = value;
}

std::string Frame::getBody()
{
    return body;
}
