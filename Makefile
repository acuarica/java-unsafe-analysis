
R=r
SRC=analysis
CSV=$(SRC)/csv
OUT=out/analysis
OUTS=$(patsubst $(SRC)/%.r,$(OUT)/%,$(wildcard $(SRC)/*.*.r))

.SECONDARY:

all: $(OUTS)

$(OUT)/cs.csv: out/unsafe-maven.csv
$(OUT)/artifacts.pdf: $(OUT)/cs.csv $(CSV)/unsafe-def-members.csv $(CSV)/unsafe-def-groups.csv
$(OUT)/classunit.pdf: $(OUT)/cs.csv $(CSV)/unsafe-def-members.csv $(CSV)/unsafe-def-groups.csv
$(OUT)/patterns.pdf: $(CSV)/comments.csv
$(OUT)/usage-maven.pdf: $(OUT)/cs.csv $(CSV)/unsafe-def-members.csv $(CSV)/unsafe-def-groups.csv
$(OUT)/usage-so.pdf: stackoverflow/results/method-usages.csv

$(OUT)/%: $(SRC)/%.r | $(OUT)
	$(R) --slave --vanilla --file=$< --args $@

$(OUT):
	mkdir -p $@

clean:
	rm -f $(OUT)
