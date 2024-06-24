# Thoth
Thoth is a command-line utility for automatically grading student submission for a particular exercise. 
It was developed as a proof-of-concept for the 41st Twente Student Conference on IT (TScIT41) 
for the module 12 Research Project (2023-2024) given at the Univeristy of Twente.

# Use case
Currently, with Thoth, you can import Remindo 'full-export' files (see `dataset/testsubmissions.csv`), 
filter on one exercise, enter a grading configuration (see `dataset/test-gradingconfig.json`), 
and run this config for all submissions for that exercise.

# Logic
Thoth parses the CSV file, filters out empty submissions, filters submissions on exercise, tries to parse the submissions with the `JavaParser` library, 
and then runs Dynamic and Static analysis for the submissions according to the grading configuration.

## Dynamic Analysis
Dynamic analysis is done by formatting the parsed submissions (`SubmissionFormatter`), compiling submissions locally at runtime with the `InMemoryJavaCompiler`, looking up the method to test and then asserting the output of the method.

## Static Analysis
Static analysis is done by taking the formatted submissions (unformatted) and passing them to the `StaticAnalyser`, which runs the required analysis methods according to the grading config.

# Output
Thoth outputs its results into a directory as `<input file name>_GRADED_EXERCISES_ex<exercise>_FULLGRADES.json` and a summary of the final grades in `<input file name>_GRADED_EXERCISES_ex<exercise>_SUMMARY.csv`.
