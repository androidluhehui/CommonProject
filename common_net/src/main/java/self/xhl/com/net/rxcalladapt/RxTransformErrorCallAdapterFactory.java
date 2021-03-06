package self.xhl.com.net.rxcalladapt;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import retrofit2.Call;
import retrofit2.CallAdapter;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import rx.Observable;
import rx.Scheduler;
import rx.functions.Func1;

/**
 * @author bingo
 * @version 1.0.0
 */
//无需修改  observable捕获异常 交给onError
public final class RxTransformErrorCallAdapterFactory extends CallAdapter.Factory{
    private final RxJavaCallAdapterFactory original;
    private RxTransformErrorCallAdapterFactory() {
        original = RxJavaCallAdapterFactory.create();
    }

    private RxTransformErrorCallAdapterFactory(Scheduler scheduler) {
        original = RxJavaCallAdapterFactory.createWithScheduler(scheduler);
    }

    static CallAdapter.Factory create() {
        return new RxTransformErrorCallAdapterFactory();
    }

    public static CallAdapter.Factory createWithScheduler(Scheduler scheduler) {
        return new RxTransformErrorCallAdapterFactory(scheduler);
    }

    @Override
    public CallAdapter<?> get(Type returnType, Annotation[] annotations, Retrofit retrofit) {
        CallAdapter<?> adapter = original.get(returnType, annotations, retrofit);
        if(adapter == null) return null;

        return new RxCallAdapterWrapper(adapter);
    }

    private static class RxCallAdapterWrapper implements CallAdapter<Observable<?>> {
        private final CallAdapter<?> wrapped;

        RxCallAdapterWrapper(CallAdapter<?> wrapped) {
            this.wrapped = wrapped;
        }

        @Override
        public Type responseType() {
            return wrapped.responseType();
        }

        @SuppressWarnings("unchecked")
        @Override
        public <R> Observable<?> adapt(Call<R> call) {
            return ((Observable) wrapped.adapt(call)).onErrorResumeNext(new Func1<Throwable, Observable>() {
                @Override
                public Observable call(Throwable throwable) {
                    return Observable.error(transformException(throwable));
                }
            });
        }

        private Throwable transformException(Throwable throwable) {
            return throwable;
        }
    }
}