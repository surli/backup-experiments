package com.puppycrawl.tools.checkstyle.checks.naming;

class InputOverrideAnnotationNoNPE
{
    // method with many parameters
    void InputOverrideAnnotationNoNPEMethod(int a, int b) {

    }

    // method with many parameters
    void InputOverrideAnnotationNoNPEMethod2(int a, int b) {

    }
}

class Test extends InputParameterNameOverrideAnnotationNoNPE
{
    @Override
    void InputOverrideAnnotationNoNPEMethod(int a, int b) {

    }

    @java.lang.Override
    void InputOverrideAnnotationNoNPEMethod2(int a, int b) {

    }
}
