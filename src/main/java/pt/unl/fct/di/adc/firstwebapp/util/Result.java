package pt.unl.fct.di.adc.firstwebapp.util;

import java.util.function.Function;
import pt.unl.fct.di.adc.firstwebapp.error.ErrorCode;

public abstract class Result<T> {
    private Result() {}

    // Much simpler signatures! No wildcards, no <E>
    public abstract <U> Result<U> flatMap(Function<T, Result<U>> mapper);
    
    public abstract <U> Result<U> map(Function<T, U> mapper);
    
    // onFailure now explicitly takes an ErrorCode
    public abstract <R> R fold(Function<T, R> onSuccess, Function<ErrorCode, R> onFailure);

    public static <T> Result<T> success(T value) {
        return new Success<>(value);
    }

    public static <T> Result<T> failure(ErrorCode error) {
        return new Failure<>(error);
    }

    // --- Implementations ---

    public static final class Success<T> extends Result<T> {
        private final T value;
        private Success(T value) { this.value = value; }

        @Override
        public <U> Result<U> flatMap(Function<T, Result<U>> mapper) { return mapper.apply(value); }

        @Override
        public <U> Result<U> map(Function<T, U> mapper) { return success(mapper.apply(value)); }

        @Override
        public <R> R fold(Function<T, R> onSuccess, Function<ErrorCode, R> onFailure) { 
            return onSuccess.apply(value); 
        }
    }

    public static final class Failure<T> extends Result<T> {
        private final ErrorCode error;
        private Failure(ErrorCode error) { this.error = error; }

        @Override
        public <U> Result<U> flatMap(Function<T, Result<U>> mapper) { return failure(error); }

        @Override
        public <U> Result<U> map(Function<T, U> mapper) { return failure(error); }

        @Override
        public <R> R fold(Function<T, R> onSuccess, Function<ErrorCode, R> onFailure) { 
            return onFailure.apply(error); 
        }
    }
}
