/*
 * Copyright (c) 2007 Mockito contributors
 * This program is made available under the terms of the MIT License.
 */
package org.mockitousage.customization;

import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.exceptions.misusing.NotAMockException;
import org.mockito.exceptions.verification.NoInteractionsWanted;
import org.mockito.exceptions.verification.VerificationInOrderFailure;
import org.mockito.exceptions.verification.WantedButNotInvoked;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.mockitousage.IMethods;
import org.mockitousage.MethodsImpl;
import org.mockitoutil.TestBase;

import java.util.Set;

import static org.mockito.BDDMockito.*;

public class BDDMockitoTest extends TestBase {

    @Mock IMethods mock;

    @Test
    public void shouldStub() throws Exception {
        given(mock.simpleMethod("foo")).willReturn("bar");

        assertEquals("bar", mock.simpleMethod("foo"));
        assertEquals(null, mock.simpleMethod("whatever"));
    }

    @Test
    public void shouldStubWithThrowable() throws Exception {
        given(mock.simpleMethod("foo")).willThrow(new RuntimeException());

        try {
            assertEquals("foo", mock.simpleMethod("foo"));
            fail();
        } catch(RuntimeException e) {}
    }

    @Test
    public void shouldStubWithThrowableClass() throws Exception {
        given(mock.simpleMethod("foo")).willThrow(RuntimeException.class);

        try {
            assertEquals("foo", mock.simpleMethod("foo"));
            fail();
        } catch(RuntimeException e) {}
    }

    @Test
    public void shouldStubWithAnswer() throws Exception {
        given(mock.simpleMethod(anyString())).willAnswer(new Answer<String>() {
            public String answer(InvocationOnMock invocation) throws Throwable {
                return (String) invocation.getArguments()[0];
            }});

        assertEquals("foo", mock.simpleMethod("foo"));
    }

    @Test
    public void shouldStubWithWillAnswerAlias() throws Exception {
        given(mock.simpleMethod(anyString())).will(new Answer<String>() {
            public String answer(InvocationOnMock invocation) throws Throwable {
                return (String) invocation.getArguments()[0];
            }});

        assertEquals("foo", mock.simpleMethod("foo"));
    }

    @Test
    public void shouldStubConsecutively() throws Exception {
       given(mock.simpleMethod(anyString()))
           .willReturn("foo")
           .willReturn("bar");

       assertEquals("foo", mock.simpleMethod("whatever"));
       assertEquals("bar", mock.simpleMethod("whatever"));
    }

    @Test
    public void shouldStubConsecutivelyWithCallRealMethod() throws Exception {
        MethodsImpl mock = mock(MethodsImpl.class);
        willReturn("foo").willCallRealMethod()
                .given(mock).simpleMethod();

       assertEquals("foo", mock.simpleMethod());
       assertEquals(null, mock.simpleMethod());
    }

    @Test
    public void shouldStubVoid() throws Exception {
        willThrow(new RuntimeException()).given(mock).voidMethod();

        try {
            mock.voidMethod();
            fail();
        } catch(RuntimeException e) {}
    }

    @Test
    public void shouldStubVoidWithExceptionClass() throws Exception {
        willThrow(RuntimeException.class).given(mock).voidMethod();

        try {
            mock.voidMethod();
            fail();
        } catch(RuntimeException e) {}
    }

    @Test
    public void shouldStubVoidConsecutively() throws Exception {
        willDoNothing()
        .willThrow(new RuntimeException())
        .given(mock).voidMethod();

        mock.voidMethod();
        try {
            mock.voidMethod();
            fail();
        } catch(RuntimeException e) {}
    }

    @Test
    public void shouldStubVoidConsecutivelyWithExceptionClass() throws Exception {
        willDoNothing()
        .willThrow(IllegalArgumentException.class)
        .given(mock).voidMethod();

        mock.voidMethod();
        try {
            mock.voidMethod();
            fail();
        } catch(IllegalArgumentException e) {}
    }

    @Test
    public void shouldStubUsingDoReturnStyle() throws Exception {
        willReturn("foo").given(mock).simpleMethod("bar");

        assertEquals(null, mock.simpleMethod("boooo"));
        assertEquals("foo", mock.simpleMethod("bar"));
    }

    @Test
    public void shouldStubUsingDoAnswerStyle() throws Exception {
        willAnswer(new Answer<String>() {
            public String answer(InvocationOnMock invocation) throws Throwable {
                return (String) invocation.getArguments()[0];
            }})
        .given(mock).simpleMethod(anyString());

        assertEquals("foo", mock.simpleMethod("foo"));
    }

    class Dog {
        public String bark() {
            return "woof";
        }
    }

    @Test
    public void shouldStubByDelegatingToRealMethod() throws Exception {
        //given
        Dog dog = mock(Dog.class);
        //when
        willCallRealMethod().given(dog).bark();
        //then
        assertEquals("woof", dog.bark());
    }

    @Test
    public void shouldStubByDelegatingToRealMethodUsingTypicalStubbingSyntax() throws Exception {
        //given
        Dog dog = mock(Dog.class);
        //when
        given(dog.bark()).willCallRealMethod();
        //then
        assertEquals("woof", dog.bark());
    }

    @Test
    public void shouldAllStubbedMockReferenceAccess() throws Exception {
        Set expectedMock = mock(Set.class);

        Set returnedMock = given(expectedMock.isEmpty()).willReturn(false).getMock();

        assertEquals(expectedMock, returnedMock);
    }

    @Test(expected = NotAMockException.class)
    public void shouldValidateMockWhenVerifying() {

        then("notMock").should();
    }

    @Test(expected = NotAMockException.class)
    public void shouldValidateMockWhenVerifyingWithExpectedNumberOfInvocations() {

        then("notMock").should(times(19));
    }

    @Test(expected = NotAMockException.class)
    public void shouldValidateMockWhenVerifyingNoMoreInteractions() {

        then("notMock").should();
    }

    @Test(expected = WantedButNotInvoked.class)
    public void shouldFailForExpectedBehaviorThatDidNotHappen() {

        then(mock).should().booleanObjectReturningMethod();
    }

    @Test
    public void shouldPassForExpectedBehaviorThatHappened() {

        mock.booleanObjectReturningMethod();

        then(mock).should().booleanObjectReturningMethod();
    }

    @Test
    public void shouldValidateThatMockDidNotHaveAnyInteractions() {

        then(mock).shouldHaveZeroInteractions();
    }

    @Test(expected = NoInteractionsWanted.class)
    public void shouldFailWhenMockHadUnwantedInteractions() {

        mock.booleanObjectReturningMethod();

        then(mock).shouldHaveZeroInteractions();
    }

    @Test
    public void shouldPassForInteractionsThatHappenedInCorrectOrder() {

        InOrder inOrder = inOrder(mock);

        mock.booleanObjectReturningMethod();
        mock.arrayReturningMethod();

        then(mock).should(inOrder).booleanObjectReturningMethod();
        then(mock).should(inOrder).arrayReturningMethod();
    }

    @Test(expected = VerificationInOrderFailure.class)
    public void shouldFailForInteractionsThatWereInWrongOrder() {

        InOrder inOrder = inOrder(mock);

        mock.arrayReturningMethod();
        mock.booleanObjectReturningMethod();

        then(mock).should(inOrder).booleanObjectReturningMethod();
        then(mock).should(inOrder).arrayReturningMethod();
    }

    @Test(expected = WantedButNotInvoked.class)
    public void shouldFailWhenCheckingOrderOfInteractionsThatDidNotHappen() {

        InOrder inOrder = inOrder(mock);

        then(mock).should(inOrder).booleanObjectReturningMethod();
    }

    @Test
    public void shouldPassFluentBddScenario() {

        Bike bike = new Bike();
        Person person = mock(Person.class);
        Police police = mock(Police.class);

        person.ride(bike);
        person.ride(bike);

        then(person).should(times(2)).ride(bike);
        then(police).shouldHaveZeroInteractions();
    }

    @Test
    public void shouldPassFluentBddScenarioWithOrderedVerification() {

        Bike bike = new Bike();
        Car car = new Car();
        Person person = mock(Person.class);
        InOrder inOrder = inOrder(person);

        person.drive(car);
        person.ride(bike);
        person.ride(bike);

        then(person).should(inOrder).drive(car);
        then(person).should(inOrder, times(2)).ride(bike);
    }

    @Test
    public void shouldPassFluentBddScenarioWithOrderedVerificationForTwoMocks() {

        Car car = new Car();
        Person person = mock(Person.class);
        Police police = mock(Police.class);
        InOrder inOrder = inOrder(person, police);

        person.drive(car);
        person.drive(car);
        police.chase(car);

        then(person).should(inOrder, times(2)).drive(car);
        then(police).should(inOrder).chase(car);
    }

    static class Person {

        void ride(Bike bike) {}

        void drive(Car car) {}
    }

    static class Bike {

    }

    static class Car {

    }

    static class Police {

        void chase(Car car) {}
    }
}
