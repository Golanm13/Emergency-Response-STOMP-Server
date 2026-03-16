CFLAGS:=-c -Wall -Weffc++ -g -std=c++11 -Iinclude
LDFLAGS:=-lboost_system -lpthread
# TODO change the makefile for StompEMIlient
all: StompEMIClient

# EchoClient: bin/ConnectionHandler.o bin/echoClient.o
# 	g++ -o bin/EchoClient bin/ConnectionHandler.o bin/echoClient.o $(LDFLAGS)

StompEMIClient: bin/event.o bin/utils.o bin/FrameEncoderDecoder.o bin/ConnectionHandler.o bin/StompProtocol.o bin/keyBoardInputHandler.o bin/ServerListener.o bin/StompClient.o
	g++ -o bin/StompEMIClient bin/ConnectionHandler.o bin/event.o bin/FrameEncoderDecoder.o bin/keyBoardInputHandler.o bin/ServerListener.o bin/StompProtocol.o bin/utils.o bin/StompClient.o $(LDFLAGS)

bin/ConnectionHandler.o: src/ConnectionHandler.cpp
	g++ $(CFLAGS) -o bin/ConnectionHandler.o src/ConnectionHandler.cpp

bin/echoClient.o: src/echoClient.cpp
	g++ $(CFLAGS) -o bin/echoClient.o src/echoClient.cpp

bin/event.o: src/event.cpp
	g++ $(CFLAGS) -o bin/event.o src/event.cpp

bin/FrameEncoderDecoder.o: src/FrameEncoderDecoder.cpp
	g++ $(CFLAGS) -o bin/FrameEncoderDecoder.o src/FrameEncoderDecoder.cpp

bin/keyBoardInputHandler.o: src/keyBoardInputHandler.cpp
	g++ $(CFLAGS) -o bin/keyBoardInputHandler.o src/keyBoardInputHandler.cpp

bin/ServerListener.o: src/ServerListener.cpp
	g++ $(CFLAGS) -o bin/ServerListener.o src/ServerListener.cpp

bin/StompProtocol.o: src/StompProtocol.cpp
	g++ $(CFLAGS) -o bin/StompProtocol.o src/StompProtocol.cpp

bin/utils.o: src/utils.cpp
	g++ $(CFLAGS) -o bin/utils.o src/utils.cpp

bin/StompClient.o: src/StompClient.cpp
	g++ $(CFLAGS) -o bin/StompClient.o src/StompClient.cpp

.PHONY: clean
clean:
	rm -f bin/*
	