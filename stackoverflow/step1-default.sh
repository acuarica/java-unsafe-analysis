if [ ! -d "results" ]; then
  mkdir results
fi
java -Xmx4G -cp unsafe-analysis-0.0.1-SNAPSHOT-jar-with-dependencies.jar ch.usi.inf.reveal.unsafe_analysis.CandidatesExtractor ./data/json ./results/candidates.csv
