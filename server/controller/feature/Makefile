.SUFFIXES : .c .o

CC = g++

LDFLAGS = libfeature.so -lm -lavresample -lavformat -lavcodec -lavutil -lpthread -lcrypto -lfftw3 -lblas

CFLAGS = -Wall

OBJS = test.o
SRCS = test.c

TARGET = extract

all : $(TARGET)

$(TARGET) : $(OBJS)
	$(CC) -o $@ $(OBJS) $(LDFLAGS)

clean :
	rm -rf $(OBJS) $(TARGET) core

new :
	$(MAKE) clean
	$(MAKE)
