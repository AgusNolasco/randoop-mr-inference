import os
import pandas as pd
import statistics
import csv
import plotly.graph_objects as go
import plotly.io as pio 

pio.kaleido.scope.mathjax = None

folders_to_process = 'from-friday/memoria/output'

def get_mut_score(df):
	killed = 0
	mutants = len(df.columns)-1
	for column in df:
		if (column=='Unnamed: 0'): 
			continue
		if (1 in df[column].unique()):
			killed += 1
	ms = round(killed/mutants*100,2)
	return ms


def get_random_df(df,n):
	if (len(df.index)==0):
		return df
	else:
		return df.sample(n)

i = 0
simulations = 101

max_selection = 11
all_avgs_random = []
all_avgs_sat = []

ms_sat_all_subj = []
for subject_name in os.listdir(folders_to_process):
	reduced_csv = f'from-friday/memoria/output/{subject_name}/allow_epa_loops_true/EPA_AWARE/1000/mutants/mutation-results-REDUCED.csv'
	reduced_df = pd.read_csv(reduced_csv)
	ms_sat = get_mut_score(reduced_df)
	ms_sat_all_subj.append(ms_sat)

for to_select in range(1, max_selection):
	all_avgs_sat.append(round(statistics.mean(ms_sat_all_subj),2))



for to_select in range(1, max_selection):
	avg_scores_random_per_subject = []
	avg_scores_sat_per_subject = []
	print(f'rows to select: {to_select}')
	for subject_name in os.listdir(folders_to_process):
		print(f'subject {subject_name}')
		inferred_csv = f'from-friday/memoria/output/{subject_name}/allow_epa_loops_true/EPA_AWARE/1000/mutants/mutation-results-INFERRED.csv'
		reduced_csv = f'from-friday/memoria/output/{subject_name}/allow_epa_loops_true/EPA_AWARE/1000/mutants/mutation-results-REDUCED.csv'
		inferred_df = pd.read_csv(inferred_csv)
		print(f'inferred shape: {inferred_df.shape}')
		reduced_df = pd.read_csv(reduced_csv)
		print(f'reduced shape: {reduced_df.shape}')

		if (to_select>=len(reduced_df.index)+1):
			continue

		mut_scores_random = []
		mut_scores_sat = []
		for num_sim in range(1,simulations):

			# Comput mut score for random
			random_df_i = get_random_df(inferred_df, to_select)
			ms_random_i = get_mut_score(random_df_i)
			mut_scores_random.append(ms_random_i)

			# Compute mut score for sat
			#sat_df = get_random_df(reduced_df, min(to_select,len(reduced_df.index)))
			#ms_sat = get_mut_score(sat_df)
			#mut_scores_sat.append(ms_sat)

		avg_mut_score_random = round(statistics.mean(mut_scores_random),2)
		print(f'scores: {mut_scores_random}')
		print(f'subject {subject_name} - random score: {avg_mut_score_random}')
		avg_scores_random_per_subject.append(avg_mut_score_random)
		#avg_mut_score_sat = round(statistics.mean(mut_scores_sat),2)
		#print(f'scores: {mut_scores_sat}')
		#print(f'subject {subject_name} - sat score: {avg_mut_score_sat}')
		#avg_scores_sat_per_subject.append(avg_mut_score_sat)
		print()
		#break

	avg_selection_random = round(statistics.mean(avg_scores_random_per_subject),2)
	print(f'mrs: {to_select} - avg mut score random: {avg_selection_random}')
	all_avgs_random.append(avg_selection_random)
	#avg_selection_sat = round(statistics.mean(avg_scores_sat_per_subject),2)
	#print(f'mrs: {to_select} - avg mut score sat: {avg_selection_sat}')
	#all_avgs_sat.append(avg_selection_sat)

print('select MRs')
selected = [to_select for to_select in range(1, max_selection)]
print(selected)
print('all avgs random')
print(all_avgs_random)
print('all avgs sat')
print(all_avgs_sat)

fig = go.Figure()

# Add traces
fig.add_trace(go.Line(x=selected, y=all_avgs_random,
                    mode='lines',
                    line = dict(dash = 'dash'),
                    name='Random'))
fig.add_trace(go.Line(x=selected, y=all_avgs_sat,
                    mode='lines',
                    line = dict(dash = 'dash'),
                    name='SAT'))

if not os.path.exists("images"):
    os.mkdir("images")

fig.update_layout(
    xaxis_title="Selected Metamorphic Relations",
    yaxis_title="Average Mutation Score",
)

fig.write_image("images/random-vs-sat-selection.pdf")





