#include "../include/utils.h"

void split_str(const std::string &str, char delimiter, std::vector<std::string> &out)
{
    std::stringstream ss(str);
    std::string token;
    while (std::getline(ss, token, delimiter))
    {
        out.push_back(token);
    }
}
