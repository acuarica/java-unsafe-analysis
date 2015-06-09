
UNAME=$(shell uname)

ifeq ($(UNAME), Darwin)
  R=r
else ifeq ($(UNAME), Linux)
  R=R
else
  $(error Unrecognized environment. Only supported Darwin and Linux)
endif

SRC=analysis
BUILD=out

OUTS=$(patsubst $(SRC)/%.r,$(BUILD)/%,$(wildcard $(SRC)/*.*.r))

OVERVIEWPRE=$(BUILD)/overview-pre.pdf
OVERVIEW=$(BUILD)/overview.pdf

OVERVIEWSOPRE=$(BUILD)/overview-so-pre.pdf
OVERVIEWSO=$(BUILD)/overview-so.pdf

.SECONDARY:

all: $(OUTS) $(OVERVIEW) $(OVERVIEWSO)

$(BUILD)/cs.csv: out/unsafe-maven.csv
$(BUILD)/overview-pre.pdf: $(BUILD)/cs.csv analysis/unsafe-def-members.csv analysis/unsafe-def-groups.csv
$(BUILD)/overview-so-pre.pdf: stackoverflow/results/method-usages.csv
$(BUILD)/artifacts.pdf: $(BUILD)/cs.csv analysis/unsafe-def-members.csv analysis/unsafe-def-groups.csv
$(BUILD)/classunit.pdf: $(BUILD)/cs.csv analysis/unsafe-def-members.csv analysis/unsafe-def-groups.csv
$(BUILD)/patterns.pdf: analysis/comments.csv

$(OVERVIEWSO): $(OVERVIEWSOPRE) analysis/so.tex
	gs -q -sDEVICE=pdfwrite -sOutputFile="$@" -dNOPAUSE -dEPSCrop -c "<</Orientation 2>> setpagedevice" -f "$<" -c quit
	latexmk -quiet -view=pdf -output-directory=out analysis/so.tex

$(OVERVIEW): $(OVERVIEWPRE)
	gs -q -sDEVICE=pdfwrite -sOutputFile="$@" -dNOPAUSE -dEPSCrop -c "<</Orientation 2>> setpagedevice" -f "$<" -c quit
	gs -q -sDEVICE=pdfwrite -sOutputFile="$(BUILD)/overview-field.pdf" -dNOPAUSE -dEPSCrop -c "<</Orientation 2>> setpagedevice" -f "$(BUILD)/overview-pre-field.pdf" -c quit

check: $(SRC)/check.r
	$(R) --slave --vanilla --file=$<
	latexmk -quiet -view=pdf -output-directory=out analysis/check.tex
	gs -q -sDEVICE=pdfwrite -sOutputFile="out/check-land.pdf" -dNOPAUSE -dEPSCrop -c "<</Orientation 2>> setpagedevice" -f "out/check.pdf" -c quit

$(BUILD)/%: $(SRC)/%.r
	$(R) --slave --vanilla --file=$< --args $@
