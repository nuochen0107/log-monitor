package com.example.implogmonitor;

import com.example.implogmonitor.enums.ExceptionType;
import com.example.implogmonitor.util.ExceptionClassifier;
import com.example.implogmonitor.util.TemplateExtractor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class AnalyzerUnitTest {
    @Test
    void normalizeShouldMaskBusinessVariables() {
        String text = TemplateExtractor.normalize("coupon send failed mobile=13800138000 orderNo=CIOS202608270001 id=90010021");
        Assertions.assertTrue(text.contains("{mobile}"));
        Assertions.assertTrue(text.contains("{bizNo}"));
        Assertions.assertTrue(text.contains("{num}"));
    }

    @Test
    void classifyShouldDetectCouponException() {
        Assertions.assertEquals(ExceptionType.COUPON, ExceptionClassifier.classify("coupon inventory not enough"));
    }
}
