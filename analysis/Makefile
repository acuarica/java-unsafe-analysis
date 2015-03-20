
R=r
BUILD=build

OUTS=$(patsubst %.r,$(BUILD)/%,$(wildcard *.*.r))

OVERVIEWPRE=$(BUILD)/overview-pre.pdf
OVERVIEW=$(BUILD)/overview.pdf

.SECONDARY:

all: $(OUTS) $(OVERVIEW)

$(BUILD)/artifacts.pdf: $(BUILD)/cs.csv csv/unsafe-def-methods.csv csv/unsafe-def-groups.csv
$(BUILD)/classunit.pdf: $(BUILD)/cs.csv csv/unsafe-def-methods.csv csv/unsafe-def-groups.csv
$(BUILD)/overview-pre.pdf: $(BUILD)/cs.csv csv/unsafe-def-methods.csv csv/unsafe-def-groups.csv
$(BUILD)/patterns.pdf: csv/comments.csv

$(OVERVIEW): $(OVERVIEWPRE)
	gs -q -sDEVICE=pdfwrite -sOutputFile="$@" -dNOPAUSE -dEPSCrop -c "<</Orientation 2>> setpagedevice" -f "$<" -c quit

$(BUILD)/%: %.r | $(BUILD)
	$(R) --slave --vanilla --file=$< --args $@

$(BUILD):
	mkdir $(BUILD)

clean:
	rm -rf $(BUILD)
