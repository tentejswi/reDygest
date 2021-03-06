package com.redygest.grok.features.extractor;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import com.redygest.commons.data.Data;
import com.redygest.commons.data.Tweet;
import com.redygest.grok.features.data.attribute.AttributeId;
import com.redygest.grok.features.data.attribute.Attributes;
import com.redygest.grok.features.data.variable.IVariable;
import com.redygest.grok.features.data.vector.FeatureVector;
import com.redygest.grok.features.data.vector.FeatureVectorCollection;
import com.redygest.grok.features.repository.FeaturesRepository;

public class EntityExtractorTest extends TestCase {

	private final IFeatureExtractor posExtractor = FeatureExtractorFactory
			.getInstance().getFeatureExtractor(FeatureExtractorType.POSFEATURE);
	private final IFeatureExtractor npentityExtractor = FeatureExtractorFactory
			.getInstance().getFeatureExtractor(FeatureExtractorType.NPENTITY);
	private final IFeatureExtractor entityExtractor = FeatureExtractorFactory
			.getInstance().getFeatureExtractor(FeatureExtractorType.ENTITY);

	@Override
	protected void setUp() {
		FeaturesRepository repository = FeaturesRepository.getInstance();
		if (FeaturesRepository.getInstance().getFeatureVector(1) == null) {
			Data d1 = new Tweet(
					"{\"text\":\"Lokpal Bill went to Washington DC.\"}", "1");
			Data d2 = new Tweet(
					"{\"text\":\"Lokpal Bill went to Washington DC.\"}", "2");
			List<Data> dataList = new ArrayList<Data>();
			dataList.add(d1);
			dataList.add(d2);
			posExtractor.extract(dataList, repository);
			npentityExtractor.extract(dataList, repository);
			entityExtractor.extract(dataList, repository);
		}
	}

	public void testEntity() {
		FeatureVector fv = FeaturesRepository.getInstance().getFeatureVector(
				FeatureVectorCollection.GLOBAL_RECORD_IDENTIFIER);
		List<IVariable> variables = fv
				.getVariablesWithAttributeType(AttributeId.ENTITY);
		for (IVariable var : variables) {
			if (var.getVariableName().equals("Lokpal Bill")) {
				assertTrue(true);
				return;
			}
		}

		fail();
	}

	public void testEntityFrequency() {
		FeatureVector fv = FeaturesRepository.getInstance().getFeatureVector(
				FeatureVectorCollection.GLOBAL_RECORD_IDENTIFIER);
		List<IVariable> variables = fv
				.getVariablesWithAttributeType(AttributeId.ENTITY);
		for (IVariable var : variables) {
			Attributes attrs = var.getVariableAttributes();
			if (attrs != null
					&& attrs.containsAttributeType(AttributeId.FREQUENCY)) {
				long freq = attrs.getAttributes(AttributeId.FREQUENCY)
						.getLong();
				assertEquals(2, freq);
				return;
			}
		}

		fail();
	}
}
