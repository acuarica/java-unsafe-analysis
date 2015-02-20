
SRC=src
BUILD=build

JARANALYSIS=$(BUILD)/jaranalysis
SRCS=$(wildcard $(SRC)/*.cpp) $(wildcard $(SRC)/*.c)
OBJS=$(SRCS:$(SRC)/%=$(BUILD)/%.o)

.PHONY: all test clean

all: $(JARANALYSIS)

test: $(JARANALYSIS)
	$(JARANALYSIS) test/kryo-2.23.0.jar
	$(JARANALYSIS) test/elasticsearch-1.4.3.jar
	$(JARANALYSIS) test/asm-3.1.jar

$(JARANALYSIS): LDFLAGS=-lz
$(JARANALYSIS): $(OBJS)
	$(CXX) $(LDFLAGS) $(CXXFLAGS) -o $@ $^

$(BUILD)/%.cpp.o: CXXFLAGS+=-std=c++11
$(BUILD)/%.cpp.o: $(SRC)/%.cpp | $(BUILD)
	$(CXX) $(CXXFLAGS) -c -o $@ $<

$(BUILD)/%.c.o: $(SRC)/%.c | $(BUILD)
	$(CC) -c -o $@ $<

$(BUILD):
	mkdir -p $@

clean:
	-rm -rf $(BUILD)
