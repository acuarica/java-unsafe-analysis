
R=r
BUILD=build

INS=$(wildcard csv/*.csv)
CSVS=$(patsubst %.r,$(BUILD)/%,$(wildcard *.csv.r))
PDFS=$(patsubst %.r,$(BUILD)/%,$(wildcard *.pdf.r))

#CS=$(BUILD)/cs.csv
#OVERVIEWPRE=$(BUILD)/overview-pre.pdf
#OVERVIEW=$(BUILD)/overview.pdf
#SUMMARY=$(BUILD)/summary.pdf
#CLASSUNIT=$(BUILD)/classunit.pdf

#.PHONY: all cs summary classunit

.SECONDARY:

#all:
#	echo $(PDFS)
all: $(PDFS)

#	$(CSVS) 

#all: cs overview summary classunit

#cs: $(CS)
#overview: $(OVERVIEW)
#summary: $(SUMMARY)
#classunit: $(CLASSUNIT)

$(BUILD)/overview.pdf: $(BUILD)/overview-pre.pdf
	gs -q -sDEVICE=pdfwrite -sOutputFile="$@" -dNOPAUSE -dEPSCrop -c "<</Orientation 2>> setpagedevice" -f "$<" -c quit

#$(CS): cs.r csv/unsafe-maven.csv csv/unsafe-def-groups.csv csv/unsafe-def-methods.csv
#$(BUILD)/cs.csv

$(BUILD)/%.pdf: %.pdf.r $(CSVS) | $(BUILD)
	$(R) --slave --vanilla --file=$< --args $@

$(BUILD)/%.csv: %.csv.r $(INS) | $(BUILD)
	$(R) --slave --vanilla --file=$< --args $@

$(BUILD):
	mkdir $(BUILD)

clean:
	rm -rf $(BUILD)
