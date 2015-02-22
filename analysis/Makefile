
R=r
BUILD=build

CSV=unsafe-maven.csv
PDF=$(CSV:%.csv=$(BUILD)/%-plot-cs-flip.pdf)

.SECONDARY:

all: $(PDF)

$(BUILD)/%-plot-cs-flip.pdf: $(BUILD)/%-plot-cs.pdf
	gs -sDEVICE=pdfwrite -sOutputFile="$@" -dNOPAUSE -dEPSCrop -c "<</Orientation 2>> setpagedevice" -f "$<" -c quit

$(BUILD)/%-plot-cs.pdf: %.csv unsafe-def-groups.csv unsafe-def-methods.csv unsafe.r | $(BUILD)
	$(R) --slave --vanilla --file=unsafe.r --args $<

$(BUILD):
	mkdir $(BUILD)

clean:
	rm -rf $(BUILD)
