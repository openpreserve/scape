#include "VisualBOW.h"

VisualBOW::VisualBOW(void)
{
}

VisualBOW::~VisualBOW(void)
{
}

void VisualBOW::createVocabulary(Mat &descriptors)
{
	BOWKMeansTrainer bowtrainer(1000);
	vocabulary = bowtrainer.cluster(descriptors);	
}

Mat VisualBOW::getVocabulary(void)
{
	return vocabulary;
}

void VisualBOW::release(void)
{
	vocabulary.release();
}