.PHONY: all build clean run dist

PAUSE=.2

all: build

build: *.java
	@echo -ne "Building... "
	@javac *.java
	@echo "done"

run: Env.class
	@java Env $(PAUSE)

Env.class: build

clean:
	@$(RM) *.class

dist: clean
	@$(RM) *.zip
	@zip MAS8_Mihai_Maruseac *

