#!/usr/bin/env bash
# Run the suite and build the Allure report, KEEPING THE HISTORY so the report shows trends across runs.
#   ./allure-report.sh -Denv=campus          (any mvn test options; run it twice to see a trend)
# Report: target/site/allure-maven-plugin/index.html      History between runs: .allure-history/ (gitignored)
set -u
rm -rf target/allure-results                       # results of THIS run only
mvn -B test "$@"; status=$?                        # a red run still gets a report
mkdir -p target/allure-results
[ -d .allure-history ] && cp -r .allure-history target/allure-results/history   # last run's history in
mvn -B -q allure:report                            # results + history -> HTML report
rm -rf .allure-history && cp -r target/site/allure-maven-plugin/history .allure-history   # keep it for next time
echo "Allure report: target/site/allure-maven-plugin/index.html"
exit $status
