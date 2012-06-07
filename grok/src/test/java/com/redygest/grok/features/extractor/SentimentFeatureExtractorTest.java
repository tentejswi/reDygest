package com.redygest.grok.features.extractor;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import com.redygest.commons.data.Data;
import com.redygest.commons.data.Tweet;
import com.redygest.grok.features.data.attribute.AttributeId;
import com.redygest.grok.features.data.attribute.Attributes;
import com.redygest.grok.features.data.variable.DataVariable;
import com.redygest.grok.features.data.variable.Variable;
import com.redygest.grok.features.data.vector.FeatureVector;
import com.redygest.grok.features.data.vector.FeatureVectorCollection;
import com.redygest.grok.features.repository.FeaturesRepository;

public class SentimentFeatureExtractorTest extends TestCase {

	private IFeatureExtractor posExtractor = FeatureExtractorFactory
			.getInstance().getFeatureExtractor(FeatureExtractorType.POSFEATURE);
	private IFeatureExtractor extractor = FeatureExtractorFactory.getInstance()
			.getFeatureExtractor(FeatureExtractorType.SENTIMENTFEATURE);

	private FeatureVectorCollection f = null;

	protected void setUp() {
		if (f == null) {
			Data d1 = new Tweet(
					"{\"text\":\"an abundant supply of water in these lush gardens\"}",
					"1");
			List<Data> dataList = new ArrayList<Data>();
			dataList.add(d1);
			FeaturesRepository repository = FeaturesRepository.getInstance();
			repository.addFeatures(posExtractor.extract(dataList, repository));
			f = extractor.extract(dataList, repository);
		}
	}

	protected void tearDown() {
		// do nothing
	}

	public void testSentiment() {
		FeatureVector fv = f.getFeatureVector(1);
		Variable var = fv.getVariable(new DataVariable("abundant", 1L));
		if (var != null) {
			Attributes attrs = var.getVariableAttributes();
			List<String> tags = attrs
					.getAttributeNames(AttributeId.SENTIMENT);
			if (tags != null && tags.size() > 0) {
				assertEquals("weak_negative", tags.get(0));
				return;
			}
		}

		fail();
	}

	public void testSentimentCount() {
		FeatureVector fv = f.getFeatureVector(1);
		Variable var = fv.getVariable(new DataVariable("weak_negative", 1L));
		if (var != null) {
			Attributes attrs = var.getVariableAttributes();
			List<String> tags = attrs
					.getAttributeNames(AttributeId.SENTIMENTCOUNT);
			if (tags != null && tags.size() > 0) {
				assertEquals("2", tags.get(0));
				return;
			}
		}

		fail();
	}

}
