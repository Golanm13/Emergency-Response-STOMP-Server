#include "../include/StompProtocol.h"
#include "../include/keyboardInputHandler.h"
#include "../include/ServerListener.h"

int main(int argc, char *argv[])
{
	std::atomic<bool> *isLoggedIn = new std::atomic<bool>(false);
	std::condition_variable cv;
	std::mutex mtx;
	cout << "Starting client" << endl;
	string input;

	StompProtocol *stompProtocol = new StompProtocol(*isLoggedIn, cv, mtx);
	KeyboardInputHandler *keyboardInputHandler = new KeyboardInputHandler(*stompProtocol, *isLoggedIn, cv, mtx);
	keyboardInputHandler->start();
	return 0;
}
