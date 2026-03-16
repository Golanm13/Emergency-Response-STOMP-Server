// keyboardInput.h
#ifndef KEYBOARDINPUT_H
#define KEYBOARDINPUT_H

#include <sstream>
#include <vector>
#include <string>

void split_str(const std::string& str, char delimiter, std::vector<std::string>& out) {
    std::stringstream ss(str);
    std::string token;
    while (std::getline(ss, token, delimiter)) {
        out.push_back(token);
    }
}

#endif
