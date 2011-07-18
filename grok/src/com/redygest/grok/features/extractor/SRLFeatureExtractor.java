/**
 * 
 */
package com.redygest.grok.features.extractor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.redygest.commons.data.Data;
import com.redygest.commons.data.DataType;
import com.redygest.grok.features.computation.Features;
import com.redygest.grok.features.datatype.AttributeType;
import com.redygest.grok.features.datatype.Attributes;
import com.redygest.grok.features.datatype.DataVariable;
import com.redygest.grok.features.datatype.FeatureVector;
import com.redygest.grok.features.datatype.Variable;
import com.redygest.grok.knowledge.Event;
import com.redygest.grok.repository.FeaturesRepository;
import com.redygest.grok.repository.IFeaturesRepository;
import com.redygest.grok.srl.Senna;
import com.redygest.grok.srl.Verb;

/**
 * SRL Feature Extractor Class
 */
public class SRLFeatureExtractor extends AbstractFeatureExtractor {

	private static Senna senna = new Senna(config.getSennaPath());

	@Override
	public Features extract(List<Data> dataList) {
		Features features = new Features();
		for (Data t : dataList) {
			features.addGlobalFeatures(extract(t), true);
		}
		return features;
	}
	
	private AttributeType getAttrForLabel(String label) {
		if(label.contains("MNR")) {
			return AttributeType.SRL_MNR;
		} else if(label.contains("LOC")) {
			return AttributeType.SRL_LOC;
		} else if(label.contains("TMP")) {
			return AttributeType.SRL_TMP;
		} else if(label.contains("A0")) {
			return AttributeType.SRL_A0;
		} else if(label.contains("A1")) {
			return AttributeType.SRL_A1;
		} else if(label.contains("A2")) {
			return AttributeType.SRL_A2;
		} else if(label.contains("PNC")) {
			return AttributeType.SRL_PNC;
		} else {
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.redygest.grok.features.extractor.IFeatureExtractor#extract(com.redygest
	 * .commons.data.Tweet)
	 */
	@Override
	public FeatureVector extract(Data t) {
		FeatureVector fVector = new FeatureVector();
		IFeaturesRepository repository = FeaturesRepository.getInstance();

		String id = t.getValue(DataType.RECORD_IDENTIFIER);
		List<Verb> verbs = senna.getVerbs((t.getValue(DataType.BODY)));
		FeatureVector recordFVector = repository.getFeature(id);

		// add semantic role labels as DataVariables for Sentence
		for (Verb v : verbs) {
			String srlId = "SRL_" + v.getIndex();
			Variable var = recordFVector.getVariable(new DataVariable(srlId,
					Long.valueOf(id)));
			if(var == null) {
				var = new DataVariable(srlId, Long.valueOf(id));
			}
			
			Attributes attrs = var.getVariableAttributes();
			HashMap<String, List<String>> args = v.getArgumentToText();
			if(args != null) {
				for(String key : args.keySet()) {
					List<String> values = args.get(key);
					for(String value : values) {
						AttributeType type = getAttrForLabel(key);
						if(type != null) {
							attrs.put(value, type);
						}
					}
				}
			}
			
			fVector.addVariable(var);
		}

		return fVector;
	}

}
