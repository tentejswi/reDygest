package com.entityextractionevaluation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.redygest.commons.config.ConfigReader;
import com.redygest.commons.data.Data;
import com.redygest.commons.data.DataType;
import com.redygest.commons.data.Tweet;
import com.redygest.commons.preprocessor.twitter.ITweetPreprocessor;
import com.redygest.commons.util.Counter;
import com.redygest.commons.util.CounterMap;
import com.redygest.commons.util.PriorityQueue;
import com.redygest.grok.features.computation.FeaturesComputation;
import com.redygest.grok.features.datatype.AttributeType;
import com.redygest.grok.features.datatype.Attributes;
import com.redygest.grok.features.datatype.FeatureVector;
import com.redygest.grok.features.datatype.Variable;
import com.redygest.grok.features.repository.FeaturesRepository;
import com.redygest.grok.prefilter.PrefilterRunner;

/**
 * 
 * @author tejaswi contains all counts that the Filtering algo can use
 */
public class EvaluationMetrics {

	protected List<Data> tweets;
	protected ITweetPreprocessor preprocessor = null;
	protected PrefilterRunner prefilterRunner = null;

	public Counter<String> np_counts = new Counter<String>();
	public Counter<String> ner_counts = new Counter<String>();

	public CounterMap<String, String> entity_Cooccurance = new CounterMap<String, String>();
	public CounterMap<String, String> ner_ner_entityCooccurance = new CounterMap<String, String>();
	public CounterMap<String, String> np_ner_entityCooccurance = new CounterMap<String, String>();

	private FeaturesRepository repository = null;

	private static EvaluationMetrics em = null;

	public static EvaluationMetrics getInstance(String file) {
		if (em == null) {
			em = new EvaluationMetrics(file);
			return em;
		} else {
			return em;
		}
	}

	protected final void read(String file) {
		this.tweets = new ArrayList<Data>();
		try {
			BufferedReader rdr = new BufferedReader(new FileReader(new File(
					file)));
			String line;
			long i = 0;
			while ((line = rdr.readLine()) != null) {
				try {
					boolean pass = true;
					Tweet t = new Tweet(line, String.valueOf(i), preprocessor);
					// prefilter code
					if (prefilterRunner != null) {
						pass = prefilterRunner.runFilters(t
								.getValue(DataType.ORIGINAL_TEXT));
					}

					if (pass && t.getValue(DataType.BODY) != null) {
						this.tweets.add(t);
					}

					i++;
				} catch (Exception e) {
					continue;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private EvaluationMetrics(String file) {
		read(file);
		ConfigReader conf = ConfigReader.getInstance();
		FeaturesComputation fc = new FeaturesComputation(conf
				.getExtractorsList());
		try {
			this.repository = fc.computeFeatures(tweets);
		} catch (Exception e) {
			e.printStackTrace();
		}

		runMetrics();
	}

	private void runMetrics() {
		for (Data t : tweets) {
			Set<String> np_entities = new HashSet<String>();
			Set<String> ner_entities = new HashSet<String>();

			String id = t.getValue(DataType.RECORD_IDENTIFIER);
			FeatureVector fv = this.repository.getFeatureVector(id);

			// collect NP entities
			for (Variable v : fv
					.getVariablesWithAttributeType(AttributeType.NPENTITY)) {
				Attributes attrs = v.getVariableAttributes();
				List<String> attrNames = attrs
						.getAttributeNames(AttributeType.SYNONYM);
				if (attrNames != null && attrNames.size() > 0) {
					np_entities.add(attrNames.get(0).toLowerCase());
					np_counts.incrementCount(attrNames.get(0).toLowerCase(),
							1.0);
				} else {
					np_entities.add(v.getVariableName().toLowerCase());
					np_counts.incrementCount(v.getVariableName().toLowerCase(),
							1.0);
				}
			}

			// collect NERs
			for (Variable v : fv
					.getVariablesWithAttributeType(AttributeType.NER_CLASS)) {
				Attributes attrs = v.getVariableAttributes();
				List<String> attrNames = attrs
						.getAttributeNames(AttributeType.SYNONYM);
				if (attrNames != null && attrNames.size() > 0) {
					ner_entities.add(attrNames.get(0).toLowerCase());
					ner_counts.incrementCount(attrNames.get(0).toLowerCase(),
							1.0);
				} else {
					ner_entities.add(v.getVariableName().toLowerCase());
					ner_counts.incrementCount(
							v.getVariableName().toLowerCase(), 1.0);
				}
			}

			List<String> ner_ents = new ArrayList<String>(ner_entities);
			for (int i = 0; i < ner_ents.size(); i++) {
				for (int j = i + 1; j < ner_ents.size(); j++) {
					ner_ner_entityCooccurance.incrementCount(ner_ents.get(i),
							ner_ents.get(j), 1.0);
				}
			}

			List<String> np_ents = new ArrayList<String>(np_entities);
			for (int i = 0; i < np_ents.size(); i++) {
				for (int j = 0; j < ner_ents.size(); j++) {
					np_ner_entityCooccurance.incrementCount(np_ents.get(i),
							ner_ents.get(j), 1.0);
				}
			}

		}

	}

	private HashMap<String, Double> getTopN(Counter<String> counts, int n) {
		HashMap<String, Double> topN = new HashMap<String, Double>();
		PriorityQueue<String> pq = counts.asPriorityQueue();
		int count = 0;
		while (pq.hasNext() && count < n) {
			String key = pq.next();
			topN.put(key, counts.getCount(key));
		}
		return topN;
	}

}