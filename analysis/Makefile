
R=r
BUILD=build

INS=$(wildcard csv/*.csv)
CSVS=$(patsubst %.r,$(BUILD)/%,$(wildcard *.csv.r))
PDFS=$(patsubst %.r,$(BUILD)/%,$(wildcard *.pdf.r))

OVERVIEWPRE=$(BUILD)/overview-pre.pdf
OVERVIEW=$(BUILD)/overview.pdf

.SECONDARY:

all: $(PDFS) $(OVERVIEW)

$(OVERVIEW): $(OVERVIEWPRE)
	gs -q -sDEVICE=pdfwrite -sOutputFile="$@" -dNOPAUSE -dEPSCrop -c "<</Orientation 2>> setpagedevice" -f "$<" -c quit

$(BUILD)/%.pdf: %.pdf.r $(CSVS) | $(BUILD)
	$(R) --slave --vanilla --file=$< --args $@

$(BUILD)/%.csv: %.csv.r $(INS) | $(BUILD)
	$(R) --slave --vanilla --file=$< --args $@

$(BUILD):
	mkdir $(BUILD)

clean:
	rm -rf $(BUILD)
