
R=r
BUILD=build

OUTS=$(patsubst %.r,$(BUILD)/%,$(wildcard *.*.r))

OVERVIEWPRE=$(BUILD)/overview-pre.pdf
OVERVIEW=$(BUILD)/overview.pdf

OVERVIEWSOPRE=$(BUILD)/overview-so-pre.pdf
OVERVIEWSO=$(BUILD)/overview-so.pdf

OVERVIEWJDK8PRE=$(BUILD)/overview-jdk8-pre.pdf
OVERVIEWJDK8=$(BUILD)/overview-jdk8.pdf

.SECONDARY:

all: $(OUTS) $(OVERVIEW) $(OVERVIEWSO) $(OVERVIEWJDK8)

$(BUILD)/cs.csv: csv/unsafe-maven.csv
$(BUILD)/cs-jdk8.csv: csv/unsafe-maven-jdk8.csv
$(BUILD)/overview-pre.pdf: $(BUILD)/cs.csv csv/unsafe-def-members.csv csv/unsafe-def-groups.csv
$(BUILD)/overview-so-pre.pdf: csv/so-method-usages.csv
$(BUILD)/overview-jdk8-pre.pdf: $(BUILD)/cs-jdk8.csv csv/unsafe-def-members.csv csv/unsafe-def-groups.csv
$(BUILD)/artifacts.pdf: $(BUILD)/cs.csv csv/unsafe-def-members.csv csv/unsafe-def-groups.csv
$(BUILD)/classunit.pdf: $(BUILD)/cs.csv csv/unsafe-def-members.csv csv/unsafe-def-groups.csv
$(BUILD)/patterns.pdf: csv/comments.csv

$(OVERVIEWJDK8): $(OVERVIEWJDK8PRE) 
	gs -q -sDEVICE=pdfwrite -sOutputFile="$@" -dNOPAUSE -dEPSCrop -c "<</Orientation 2>> setpagedevice" -f "$<" -c quit

$(OVERVIEWSO): $(OVERVIEWSOPRE) so.tex
	gs -q -sDEVICE=pdfwrite -sOutputFile="$@" -dNOPAUSE -dEPSCrop -c "<</Orientation 2>> setpagedevice" -f "$<" -c quit
	latexmk -view=pdf so.tex

$(OVERVIEW): $(OVERVIEWPRE)
	gs -q -sDEVICE=pdfwrite -sOutputFile="$@" -dNOPAUSE -dEPSCrop -c "<</Orientation 2>> setpagedevice" -f "$<" -c quit
	gs -q -sDEVICE=pdfwrite -sOutputFile="$(BUILD)/overview-field.pdf" -dNOPAUSE -dEPSCrop -c "<</Orientation 2>> setpagedevice" -f "$(BUILD)/overview-pre-field.pdf" -c quit

$(BUILD)/%: %.r | $(BUILD)
	$(R) --slave --vanilla --file=$< --args $@

$(BUILD):
	mkdir $(BUILD)

clean:
	rm -rf $(BUILD)
