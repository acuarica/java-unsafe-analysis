
R=r
BUILD=build

CSV=unsafe-maven.csv
PDF=$(CSV:%.csv=$(BUILD)/%-cs-all-vertical.pdf)

.SECONDARY:

all: $(PDF)

$(BUILD)/%-cs-all-vertical.pdf: $(BUILD)/%-cs-all-vertical-pre.pdf
	gs -q -sDEVICE=pdfwrite -sOutputFile="$@" -dNOPAUSE -dEPSCrop -c "<</Orientation 2>> setpagedevice" -f "$<" -c quit

$(BUILD)/%-cs-all-vertical-pre.pdf: %.csv unsafe-def-groups.csv unsafe-def-methods.csv unsafe.r | $(BUILD)
	$(R) --slave --vanilla --file=unsafe.r --args $<

$(BUILD):
	mkdir $(BUILD)

clean:
	rm -rf $(BUILD)
