import sys
import pandas as pd
import glob
import os
import csv

# Usage: python3 process-mutation-analysis.py {allow_epa_loops} {mrs_to_fuzz}

allow_epa_loops = sys.argv[1]
mrs_to_fuzz = sys.argv[2]

output_dir = 'output/'

mutation_summary_file = 'output/mutation-analysis-summary.csv'

data = []
for subject in os.listdir(output_dir):
	dir_to_loop = output_dir + subject + "/allow_epa_loops_{}/EPA_AWARE/{}/".format(allow_epa_loops,mrs_to_fuzz)
	for filename in glob.iglob(dir_to_loop + '**/mutation-results*', recursive=True):
		print(f'> Procesing file: {filename}')
		mrs_type = filename.split('-')[2].split('.')[0]
		df = pd.read_csv(filename)
		# Mutation score
		all_mutants = len(df.columns) - 1
		# Adding row times_killed containing the number of times each mutant was killed 
		times_killed = pd.Series(df.iloc[: , 1:].sum(axis=0))
		df.loc['times_killed'] = pd.concat([pd.Series([0]),times_killed])
		killed_mutants = (times_killed > 0).sum()
		mut_score = (killed_mutants * 100 / all_mutants)
		print(f'{subject},{allow_epa_loops},{mrs_to_fuzz},{mrs_type},{all_mutants},{killed_mutants},{"%.1f" % mut_score}')
		data.append([subject,allow_epa_loops, mrs_to_fuzz, mrs_type, all_mutants, killed_mutants, "%.1f" % mut_score])
		print()

mutation_summary_header = ['Subject','epa_loops','mrs_to_fuzz','mrs_type','all_mutants','killed','mut_score']
with open(mutation_summary_file, 'w', encoding='UTF8') as f:
    writer = csv.writer(f)
    writer.writerow(mutation_summary_header)
    writer.writerows(data)

print(f'output file: {mutation_summary_file}')
print()
print('Done!')
