package de.syngenio.demo7;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.exceptions.verification.VerificationInOrderFailure;

public class TestDelegator {

	private Delegator _delegator;
	private Delegate _delegate;

	@Before
	public void setUp() throws Exception {
		_delegate = mock(Delegate.class);
		_delegator = new Delegator(_delegate);
	}

	@Test
	public void invalidTestForCorrectSequence() {
		_delegator.doSomethingComplex();
		
		verify(_delegate).a();
		verify(_delegate).b();
		verify(_delegate).c();
		verify(_delegate).d();
	}

	@Test(expected=VerificationInOrderFailure.class)
	public void testForExceptionIfIncorrectSequence() {
		InOrder inorder = inOrder(_delegate);
		
		_delegator.doSomethingComplex();
		
		inorder.verify(_delegate).a();
		inorder.verify(_delegate).b();
		inorder.verify(_delegate).c();
		inorder.verify(_delegate).d();
	}

	@Test
	public void testForCorrectSequence() {
		InOrder inorder = inOrder(_delegate);
		
		_delegator.doSomethingComplex();
		
		inorder.verify(_delegate).d();
		inorder.verify(_delegate).c();
		inorder.verify(_delegate).b();
		inorder.verify(_delegate).a();
	}

}
