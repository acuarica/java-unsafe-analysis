
BUILD=build

CSV=depgraph.csv
DOT=$(CSV:%.csv=$(BUILD)/%.dot)
PDF=$(DOT:%.dot=%.pdf)

.SECONDARY:

all: $(PDF)

$(BUILD)/%.pdf: $(BUILD)/%.dot
	dot -Tpdf $< > $@

$(BUILD)/%.dot: %.csv %.py | $(BUILD)
	cat $< | ./$*.py > $@

$(BUILD):
	mkdir $(BUILD)

clean:
	rm -rf $(BUILD)
