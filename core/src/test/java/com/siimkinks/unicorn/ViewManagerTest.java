package com.siimkinks.unicorn;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;
import static com.siimkinks.unicorn.ContentViewContract.LifecycleEvent.STOP;
import static com.siimkinks.unicorn.ContentViewContract.LifecycleEvent.START;
import static com.siimkinks.unicorn.MockView.createNewView;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class ViewManagerTest {

    @Mock
    LayoutInflater inflater;
    @Mock
    ViewGroup rootView;
    @Mock
    ViewGroup mockView;
    @Mock
    DependencyGraphProvider graphProvider;

    ViewManager<DependencyGraphProvider> viewManager;
    MockRootActivity rootActivity;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        viewManager = spy(new ViewManager<>(graphProvider));
        doNothing().when(viewManager).transitionBetween(any(NavigationDetails.class), any(NavigationDetails.class));
        rootActivity = spy(new MockRootActivity());
        when(rootView.getChildAt(0)).thenReturn(mockView);
    }

    @Test
    public void removeViewFromTop() {
        List<MockView> views = pushThreeViewsToStack();
        final MockView remove = views.get(2);

        final Dual<NavigationDetails, Boolean> removedViewMetadata = viewManager.removeViewFromStack(remove);
        assertThat(removedViewMetadata).isNotNull();
        assertThat(removedViewMetadata.first().view()).isEqualTo(remove);
        assertThat(removedViewMetadata.second()).isEqualTo(Boolean.TRUE);
    }

    @Test
    public void removeViewFromMiddle() {
        List<MockView> views = pushThreeViewsToStack();
        final MockView remove = views.get(1);

        final Dual<NavigationDetails, Boolean> removedViewMetadata = viewManager.removeViewFromStack(remove);
        assertThat(removedViewMetadata).isNotNull();
        assertThat(removedViewMetadata.first().view()).isEqualTo(remove);
        assertThat(removedViewMetadata.second()).isEqualTo(Boolean.FALSE);
    }

    @Test
    public void removeViewFromBottom() {
        List<MockView> views = pushThreeViewsToStack();
        final MockView remove = views.get(0);

        final Dual<NavigationDetails, Boolean> removedViewMetadata = viewManager.removeViewFromStack(remove);
        assertThat(removedViewMetadata).isNotNull();
        assertThat(removedViewMetadata.first().view()).isEqualTo(remove);
        assertThat(removedViewMetadata.second()).isEqualTo(Boolean.FALSE);
    }

    @Test
    public void removeViewFromEmptyStack() {
        MockView remove = MockView.createNewView();

        final Dual<NavigationDetails, Boolean> removedViewMetadata = viewManager.removeViewFromStack(remove);
        assertThat(removedViewMetadata).isNull();
    }

    @Test
    public void removeViewWhenItIsOnlyViewInStack() {
        MockView remove = MockView.createNewView();
        viewManager.viewStack.push(remove.create());

        final Dual<NavigationDetails, Boolean> removedViewMetadata = viewManager.removeViewFromStack(remove);
        assertThat(removedViewMetadata).isNotNull();
        assertThat(removedViewMetadata.first().view()).isEqualTo(remove);
        assertThat(removedViewMetadata.second()).isEqualTo(Boolean.TRUE);
    }

    @Test
    public void removeNotExistingView() {
        pushThreeViewsToStack();
        MockView remove = MockView.createNewView();

        final Dual<NavigationDetails, Boolean> removedViewMetadata = viewManager.removeViewFromStack(remove);
        assertThat(removedViewMetadata).isNull();
    }

    @Test
    public void removeNotExistingViewFromEmptyStack() {
        MockView remove = MockView.createNewView();

        final Dual<NavigationDetails, Boolean> removedViewMetadata = viewManager.removeViewFromStack(remove);
        assertThat(removedViewMetadata).isNull();
    }

    @Test
    public void firstViewHasCorrectLifecycle() {
        final MockView firstView = rootActivity.firstView;
        final InOrder callOrder = inOrder(firstView);
        mockActivity();

        callOrder.verify(firstView).getViewResId();
        callOrder.verify(firstView).onCreate(graphProvider);
        callOrder.verify(firstView).onStart();

        assertThat(firstView.getRootView()).isNotNull();
    }

    @Test
    public void secondViewHasCorrectLifecycle() {
        final MockView firstView = rootActivity.firstView;
        final InOrder firstViewCallOrder = inOrder(firstView);
        mockActivity();

        final MockView mockView = MockView.createNewView();
        final InOrder newViewCallOrder = inOrder(mockView);

        // add
        viewManager.navigate(mockView.create());

        firstViewCallOrder.verify(firstView).onStop();
        newViewCallOrder.verify(mockView).onCreate(graphProvider);
        newViewCallOrder.verify(mockView).onStart();
        assertThat(viewManager.viewStack.size()).isEqualTo(2);

        // finish
        viewManager.finish(mockView);

        newViewCallOrder.verify(mockView).onStop();
        newViewCallOrder.verify(mockView).onDestroy();
        firstViewCallOrder.verify(firstView).onStart();
        assertThat(viewManager.viewStack.size()).isEqualTo(1);
    }

    @Test
    public void backWorksAsExpected() {
        final MockView firstView = rootActivity.firstView;
        final InOrder firstViewCallOrder = inOrder(firstView);
        mockActivity();

        final MockView mockView = MockView.createNewView();
        final InOrder newViewCallOrder = inOrder(mockView);

        // add
        viewManager.navigate(mockView.create());

        assertThat(viewManager.viewStack.size()).isEqualTo(2);

        // finish second view
        viewManager.handleBackPress();

        newViewCallOrder.verify(mockView).onStop();
        newViewCallOrder.verify(mockView).onDestroy();
        firstViewCallOrder.verify(firstView).onStart();
        assertThat(viewManager.viewStack.size()).isEqualTo(1);

        // from now on back press should only notify top view of back press, but will not finish it
        reset(firstView, mockView);

        viewManager.handleBackPress();
        assertThat(viewManager.viewStack.size()).isEqualTo(1);
        viewManager.handleBackPress();
        assertThat(viewManager.viewStack.size()).isEqualTo(1);

        newViewCallOrder.verifyNoMoreInteractions();
        verify(firstView, times(2)).onBackPressed();
        verifyNoMoreInteractions(firstView);
    }

    @Test
    public void finishNonTopView() {
        final MockView firstView = rootActivity.firstView;
        mockActivity();

        final MockView mockView = MockView.createNewView();

        // add
        final NavigationDetails secondViewNavDetails = mockView.create();
        viewManager.navigate(secondViewNavDetails);

        assertThat(viewManager.viewStack.size()).isEqualTo(2);

        reset(firstView, mockView);
        final InOrder firstViewCallOrder = inOrder(firstView);

        // finish bottom view
        viewManager.finish(firstView);

        firstViewCallOrder.verify(firstView).onDestroy();
        verifyNoMoreInteractions(mockView);
        assertThat(viewManager.viewStack.size()).isEqualTo(1);
        assertThat(viewManager.viewStack.peek()).isEqualTo(secondViewNavDetails);
    }

    @Test
    public void finishSecondTopView() {
        mockActivity();
        final List<MockView> mockViews = pushThreeMockedViewsToStack();
        assertThat(viewManager.viewStack.size()).isEqualTo(4);

        final MockView prevTopView = mockViews.get(mockViews.size() - 1);
        final MockView newTopView = MockView.createNewView();
        final NavigationDetails newTopNavDetails = newTopView.create();
        reset(newTopView, prevTopView);
        final InOrder prevTopViewCallOrder = inOrder(prevTopView);
        final InOrder newTopViewCallOrder = inOrder(newTopView);

        // navigate new view on top and finish prev top (aka second top view)
        viewManager.navigate(newTopNavDetails);
        viewManager.finish(prevTopView);

        newTopViewCallOrder.verify(newTopView).onCreate(any(DependencyGraphProvider.class));
        newTopViewCallOrder.verify(newTopView).latestLifecycleEvent();
        newTopViewCallOrder.verify(newTopView).onStart();
        verifyNoMoreInteractions(newTopView);

        prevTopViewCallOrder.verify(prevTopView).onStop();
        prevTopViewCallOrder.verify(prevTopView).latestLifecycleEvent();
        prevTopViewCallOrder.verify(prevTopView).onDestroy();
        verifyNoMoreInteractions(prevTopView);
        assertThat(viewManager.viewStack.size()).isEqualTo(4);
        assertThat(viewManager.viewStack.peek()).isEqualTo(newTopNavDetails);
    }

    @Test
    public void finishLastView() {
        final MockView firstView = rootActivity.firstView;
        mockActivity();
        assertThat(viewManager.viewStack.size()).isEqualTo(1);
        reset(firstView);

        viewManager.finish(firstView);

        verify(firstView).onStop();
        verify(firstView).onDestroy();
        verify(firstView).latestLifecycleEvent();
        verifyNoMoreInteractions(firstView);
        assertThat(viewManager.viewStack.size()).isEqualTo(0);
    }

    @Test
    public void finishViewThatIsNotInStack() {
        final MockView firstView = rootActivity.firstView;
        mockActivity();
        assertThat(viewManager.viewStack.size()).isEqualTo(1);

        reset(firstView);
        final MockView newView = MockView.createNewView();

        viewManager.finish(newView);
        verifyNoMoreInteractions(firstView);
        verifyNoMoreInteractions(newView);
        assertThat(viewManager.viewStack.size()).isEqualTo(1);
    }

    @Test
    public void navigateToViewWithClearStack() {
        final MockView firstView = rootActivity.firstView;
        mockActivity();
        final MockView secondView = MockView.createNewView();
        final NavigationDetails secondViewNavDetails = secondView.create();
        viewManager.navigate(secondViewNavDetails);

        // stack has 2 views
        assertThat(viewManager.viewStack.size()).isEqualTo(2);

        reset(firstView, secondView);

        final MockView newView = MockView.createNewView();
        final InOrder newViewCallOrder = inOrder(newView);
        final NavigationDetails newViewNavDetails = NavigationDetails
                .navigateTo(newView)
                .clearStack(true)
                .build();
        viewManager.navigate(newViewNavDetails);

        assertThat(viewManager.viewStack.size()).isEqualTo(1);
        assertThat(viewManager.viewStack.peek()).isEqualTo(newViewNavDetails);
        verify(firstView).onDestroy();
        verifyNoMoreInteractions(firstView);
        verify(secondView).onDestroy();
        verifyNoMoreInteractions(secondView);
        newViewCallOrder.verify(newView).onCreate(graphProvider);
        newViewCallOrder.verify(newView).onStart();
        newViewCallOrder.verifyNoMoreInteractions();
    }

    @Test
    public void singleInstanceBringsViewToFront() {
        mockActivity();
        final EqualMockView secondView = EqualMockView.createNewView();
        final NavigationDetails secondViewNavDetails = secondView.create();
        viewManager.navigate(secondViewNavDetails);
        viewManager.navigate(MockView.createNewView().create());

        assertThat(viewManager.viewStack.size()).isEqualTo(3);

        final EqualMockView secondView2 = EqualMockView.createNewView();
        final NavigationDetails secondViewNavDetails2 = NavigationDetails
                .navigateTo(secondView2)
                .singleInstance(true)
                .build();

        viewManager.navigate(secondViewNavDetails2);

        assertThat(viewManager.viewStack.size()).isEqualTo(3);
        final NavigationDetails top = viewManager.viewStack.peek();
        assertThat(top).isEqualTo(secondViewNavDetails);
        assertThat(top.view() == secondView).isTrue();
        assertThat(top.view() != secondView2).isTrue();
    }

    @Test
    public void singleInstanceBringsViewToFrontHasCorrectLifecycle() {
        final MockView firstView = rootActivity.firstView;
        mockActivity();
        final MockView secondView = MockView.createNewView();
        final NavigationDetails secondViewNavDetails = secondView.create();
        viewManager.navigate(secondViewNavDetails);
        final MockView thirdView = MockView.createNewView();
        viewManager.navigate(thirdView.create());

        assertThat(viewManager.viewStack.size()).isEqualTo(3);

        reset(firstView, secondView, thirdView);
        final NavigationDetails secondViewNavDetails2 = NavigationDetails
                .navigateTo(secondView)
                .singleInstance(true)
                .build();

        viewManager.navigate(secondViewNavDetails2);

        assertThat(viewManager.viewStack.size()).isEqualTo(3);
        verifyNoMoreInteractions(firstView);

        verify(thirdView).onStop();
        verifyNoMoreInteractions(thirdView);

        verify(secondView).latestLifecycleEvent();
        verify(secondView).onStart();
        verifyNoMoreInteractions(secondView);
    }

    @Test
    public void singleInstanceWhileViewIsAlreadyOnTopHasNoEffect() {
        final MockView firstView = rootActivity.firstView;
        mockActivity();
        final MockView secondView = EqualMockView.createNewView();
        final NavigationDetails secondViewNavDetails = NavigationDetails
                .navigateTo(secondView)
                .singleInstance(true)
                .build();
        viewManager.navigate(secondViewNavDetails);

        assertThat(viewManager.viewStack.size()).isEqualTo(2);
        assertThat(viewManager.viewStack.peek()).isEqualTo(secondViewNavDetails);

        reset(firstView);

        final MockView secondView2 = EqualMockView.createNewView();
        final NavigationDetails secondViewNavDetails2 = NavigationDetails
                .navigateTo(secondView2)
                .singleInstance(true)
                .build();
        viewManager.navigate(secondViewNavDetails2);

        verifyNoMoreInteractions(firstView);
        assertThat(viewManager.viewStack.size()).isEqualTo(2);
        final NavigationDetails top = viewManager.viewStack.peek();
        assertThat(top).isEqualTo(secondViewNavDetails);
        assertThat(top.view() == secondView).isTrue();
        assertThat(top.view() != secondView2).isTrue();
    }

    @Test
    public void singleInstanceWhileViewIsAlreadyOnTopCallsNoLifecycleCallbacks() {
        final MockView firstView = rootActivity.firstView;
        mockActivity();
        final MockView secondView = MockView.createNewView();
        viewManager.navigate(secondView.create());

        assertThat(viewManager.viewStack.size()).isEqualTo(2);

        reset(firstView, secondView);

        final NavigationDetails secondViewNavDetails2 = NavigationDetails
                .navigateTo(secondView)
                .singleInstance(true)
                .build();
        viewManager.navigate(secondViewNavDetails2);

        verifyNoMoreInteractions(firstView);
        verifyNoMoreInteractions(secondView);
        assertThat(viewManager.viewStack.size()).isEqualTo(2);
    }

    @Test
    public void navigateToViewWithClearStackSingleInstance() {
        final MockView firstView = rootActivity.firstView;
        mockActivity();
        final MockView secondView = MockView.createNewView();
        viewManager.navigate(secondView.create());
        final MockView thirdView = MockView.createNewView();
        viewManager.navigate(thirdView.create());

        assertThat(viewManager.viewStack.size()).isEqualTo(3);

        reset(firstView, secondView);

        viewManager.navigate(NavigationDetails
                .navigateTo(secondView)
                .clearStack(true)
                .singleInstance(true)
                .build());

        verify(firstView).onDestroy();
        verify(thirdView).onDestroy();

        verify(secondView).latestLifecycleEvent();
        verify(secondView).onStart();
        verifyNoMoreInteractions(secondView);
        assertThat(viewManager.viewStack.size()).isEqualTo(1);
    }

    @Test
    public void navigateToViewWithClearStackSingleInstanceWhileViewIsOnlyOneInStack() {
        final MockView firstView = rootActivity.firstView;
        mockActivity();
        final MockView secondView = MockView.createNewView();
        final NavigationDetails secondViewNavDetails = NavigationDetails
                .navigateTo(secondView)
                .clearStack(true)
                .build();
        viewManager.navigate(secondViewNavDetails);

        assertThat(viewManager.viewStack.size()).isEqualTo(1);
        assertThat(viewManager.viewStack.peek()).isEqualTo(secondViewNavDetails);

        reset(firstView, secondView);

        viewManager.navigate(NavigationDetails
                .navigateTo(secondView)
                .clearStack(true)
                .singleInstance(true)
                .build());

        verifyNoMoreInteractions(firstView);
        verifyNoMoreInteractions(secondView);
    }

    @Test
    public void finishFromOnCreate() {
        final MockView firstView = rootActivity.firstView;
        mockActivity();
        reset(firstView);

        final MockView secondView = spy(new MockView() {
            @Override
            public void onCreate(@NonNull DependencyGraphProvider provider) {
                super.onCreate(provider);
                viewManager.finish(this);
            }
        });

        final NavigationDetails secondViewNavDetails = secondView.create();
        reset(secondView);
        viewManager.navigate(secondViewNavDetails);

        assertThat(viewManager.viewStack.size()).isEqualTo(1);
        assertThat(viewManager.viewStack.peek()).isNotEqualTo(secondViewNavDetails);
        verify(firstView).latestLifecycleEvent();
        verify(firstView).onStop();
        verify(firstView).onStart();
        verifyNoMoreInteractions(firstView);

        verify(secondView).onCreate(any(DependencyGraphProvider.class));
        verify(secondView).onStop();
        verify(secondView).onDestroy();
        verify(secondView, times(2)).latestLifecycleEvent();
        verifyNoMoreInteractions(secondView);
    }

    @Test
    public void finishFromOnStart() {
        final MockView firstView = rootActivity.firstView;
        mockActivity();
        reset(firstView);

        final MockView secondView = spy(new MockView() {
            @Override
            public void onStart() {
                super.onStart();
                viewManager.finish(this);
            }
        });

        final NavigationDetails secondViewNavDetails = secondView.create();
        reset(secondView);
        viewManager.navigate(secondViewNavDetails);

        assertThat(viewManager.viewStack.size()).isEqualTo(1);
        assertThat(viewManager.viewStack.peek()).isNotEqualTo(secondViewNavDetails);
        verify(firstView).latestLifecycleEvent();
        verify(firstView).onStop();
        verify(firstView).onStart();
        verifyNoMoreInteractions(firstView);

        verify(secondView).onCreate(any(DependencyGraphProvider.class));
        verify(secondView).onStart();
        verify(secondView).onStop();
        verify(secondView).onDestroy();
        verify(secondView, times(2)).latestLifecycleEvent();
        verifyNoMoreInteractions(secondView);
    }

    @Test
    public void finishFromOnStop() {
        final MockView firstView = rootActivity.firstView;
        mockActivity();

        final MockView secondView = spy(new MockView() {
            @Override
            public void onStop() {
                super.onStop();
                viewManager.finish(this);
            }
        });

        final NavigationDetails secondViewNavDetails = secondView.create();
        reset(secondView);
        viewManager.navigate(secondViewNavDetails);

        assertThat(viewManager.viewStack.size()).isEqualTo(2);
        assertThat(viewManager.viewStack.peek()).isEqualTo(secondViewNavDetails);

        reset(firstView, secondView);

        final MockView thirdView = spy(new MockView());
        final NavigationDetails thirdViewNavDetails = thirdView.create();
        reset(thirdView);
        viewManager.navigate(thirdViewNavDetails);

        verifyNoMoreInteractions(firstView);
        verify(secondView).onStop();
        verify(secondView).onDestroy();
        verify(secondView).latestLifecycleEvent();
        verifyNoMoreInteractions(firstView);
        verify(thirdView).onCreate(any(DependencyGraphProvider.class));
        verify(thirdView).onStart();
        verify(thirdView).latestLifecycleEvent();
        verifyNoMoreInteractions(thirdView);
    }

    @Test
    public void finishFromOnDestroyBeforeSuperDoesNothing() {
        final MockView secondView = spy(new MockView() {
            @Override
            public void onDestroy() {
                viewManager.finish(this);
                super.onDestroy();
            }
        });
        assertFinishFromOnDestroyDoesNoting(secondView);
    }

    @Test
    public void finishFromOnDestroyAfterSuperDoesNothing() {
        final MockView secondView = spy(new MockView() {
            @Override
            public void onDestroy() {
                super.onDestroy();
                viewManager.finish(this);
            }
        });
        assertFinishFromOnDestroyDoesNoting(secondView);
    }

    @Test(expected = IllegalStateException.class)
    public void viewManagerThrowsIfActivityRestartedTwice() {
        mockActivity();
        viewManager.registerActivity(rootActivity, rootActivity);
    }

    @Test
    public void stackIsRestartedWhenActivityRestarts() {
        final List<MockView> views = restartStack();

        final NavigationDetails top = viewManager.viewStack.peek();
        assertThat(top.needsRestart()).isFalse();
        final ContentViewContract topView = top.view();
        assertThat(views.get(views.size() - 1) != topView).isTrue();
        verify(topView).onCreate(any(DependencyGraphProvider.class));
        verify(topView).onStart();

        final ArrayDeque<NavigationDetails> stack = viewManager.viewStack.clone();
        stack.poll();
        for (NavigationDetails details : stack) {
            reset(details.view());
        }

        navigateBackAndCheckForReCreate();
        navigateBackAndCheckForReCreate();
        navigateBackAndCheckForReCreate();

        for (NavigationDetails details : stack) {
            //noinspection CheckResult
            verify(details.view()).copy();
        }
    }

    @Test
    public void restartStackAndNavigateToViewWithClearStack() {
        final List<MockView> views = restartStack();

        final MockView newView = spy(new MockView());
        final NavigationDetails details = NavigationDetails
                .navigateTo(newView)
                .clearStack(true)
                .build();
        viewManager.navigate(details);

        assertThat(viewManager.viewStack.size()).isEqualTo(1);
        for (int i = 0, size = views.size(); i < size - 1; i++) {
            verifyNoMoreInteractions(views.get(i));
        }
    }

    @SuppressWarnings("CheckResult")
    @Test
    public void restartStackAndNavigateToSingleInstanceViewWithClearStack() {
        final List<MockView> views = restartStack();
        final MockView topView = views.get(views.size() - 1);

        final MockView newView = spy(new MockView());
        final NavigationDetails details = NavigationDetails
                .navigateTo(newView)
                .clearStack(true)
                .singleInstance(true)
                .build();
        viewManager.navigate(details);

        verify(topView).copy();
        assertThat(viewManager.viewStack.size()).isEqualTo(1);
        for (int i = 0, size = views.size(); i < size - 1; i++) {
            verifyNoMoreInteractions(views.get(i));
        }
    }

    @Test
    public void restartStackWithNewViewAndClear() {
        restartStack();

        final MockView newView = spy(new MockView());
        final NavigationDetails details = newView.create();
        viewManager.navigate(details);

        viewManager.handleBackPress();

        navigateBackAndCheckForReCreate();
        navigateBackAndCheckForReCreate();
    }

    @Test
    public void topViewReceivesLifecycleEventsWhenParentChangesVisibility() {
        mockActivity();
        final List<MockView> views = pushThreeMockedViewsToStack();
        for (MockView view : views) {
            reset(view);
        }
        final MockView topView = views.get(views.size() - 1);

        rootActivity.emitLifecycleEvent(STOP);
        rootActivity.emitLifecycleEvent(START);

        verify(topView).onStop();
        verify(topView).onStart();

        for (int i = 0; i < views.size() - 1; i++) {
            verifyNoCallsToLifecycleCallbacks(views.get(i));
        }
    }

    private void navigateBackAndCheckForReCreate() {
        viewManager.handleBackPress();
        final NavigationDetails firstView = viewManager.viewStack.peek();
        verify(firstView.view()).onCreate(any(DependencyGraphProvider.class));
        verify(firstView.view()).onStart();
    }

    @NonNull
    private List<MockView> restartStack() {
        mockActivity();
        final List<MockView> views = pushThreeMockedViewsToStack();
        views.add(0, rootActivity.firstView);
        rootActivity.emitLifecycleEvent(STOP);
        viewManager.unregisterActivity();
        verify(viewManager.viewStack.peek().view()).onStop();
        for (MockView view : views) {
            verify(view).onDestroy();
            reset(view);
        }
        assertThat(viewManager.viewStack.size()).isEqualTo(views.size());
        final ArrayDeque<NavigationDetails> viewStack = viewManager.viewStack;
        for (NavigationDetails details : viewStack) {
            assertThat(details.needsRestart()).isTrue();
        }
        viewManager.registerActivity(rootActivity, rootActivity);
        assertThat(viewManager.viewStack.size()).isEqualTo(views.size());
        return views;
    }

    private void assertFinishFromOnDestroyDoesNoting(@NonNull MockView secondView) {
        final MockView firstView = rootActivity.firstView;
        mockActivity();
        reset(firstView);

        final NavigationDetails secondViewNavDetails = secondView.create();
        reset(secondView);
        viewManager.navigate(secondViewNavDetails);

        assertThat(viewManager.viewStack.size()).isEqualTo(2);
        assertThat(viewManager.viewStack.peek()).isEqualTo(secondViewNavDetails);

        viewManager.finish(secondView);

        verify(firstView).latestLifecycleEvent();
        verify(firstView).onStop();
        verify(firstView).onStart();
        verifyNoMoreInteractions(firstView);

        verify(secondView).onCreate(any(DependencyGraphProvider.class));
        verify(secondView).onStart();
        verify(secondView).onStop();
        verify(secondView).onDestroy();
        verify(secondView, times(2)).latestLifecycleEvent();
        verifyNoMoreInteractions(secondView);
    }

    /* helper methods */
    private void mockActivity() {
        doReturn(inflater).when(rootActivity).getLayoutInflater();
        doReturn(rootView).when(rootActivity).getContentRootView();
        viewManager.registerActivity(rootActivity, rootActivity);
        doNothing().when(viewManager).transitionBetween(any(NavigationDetails.class), any(NavigationDetails.class));
    }

    @NonNull
    private List<MockView> pushThreeViewsToStack() {
        final ArrayList<MockView> result = new ArrayList<>();

        MockView mockView1 = createNewView();
        viewManager.viewStack.push(mockView1.create());
        result.add(mockView1);

        MockView mockView2 = createNewView();
        viewManager.viewStack.push(mockView2.create());
        result.add(mockView2);

        MockView mockView3 = createNewView();
        viewManager.viewStack.push(mockView3.create());
        result.add(mockView3);
        return result;
    }

    @NonNull
    private List<MockView> pushThreeMockedViewsToStack() {
        final ArrayList<MockView> result = new ArrayList<>();

        MockView mockView1 = MockView.createNewView();
        viewManager.viewStack.push(mockView1.create());
        result.add(mockView1);

        MockView mockView2 = MockView.createNewView();
        viewManager.viewStack.push(mockView2.create());
        result.add(mockView2);

        MockView mockView3 = MockView.createNewView();
        viewManager.viewStack.push(mockView3.create());
        result.add(mockView3);
        return result;
    }

    private static void verifyNoCallsToLifecycleCallbacks(@NonNull ContentViewContract viewContract) {
        verify(viewContract, never()).onCreate(any(DependencyGraphProvider.class));
        verify(viewContract, never()).onStop();
        verify(viewContract, never()).onStart();
        verify(viewContract, never()).onDestroy();
    }
}