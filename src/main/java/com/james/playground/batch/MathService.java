package com.james.playground.batch;

import java.util.List;
import java.util.Map;
import one.util.streamex.StreamEx;
import org.springframework.stereotype.Service;

@Service
public class MathService {
  public Integer sum(List<Integer> numbers) {
    try {
      Thread.sleep(200);
    } catch (Exception ex) {

    }

    return null;

    //    return numbers.stream().mapToInt(Integer::intValue).sum();
  }

  public int sumExceptionally(List<Integer> numbers) {
    throw new RuntimeException("Exception thrown from MathService.sumExceptionally()");
  }

  public int multiplyByTwo(int number) {
    return number * 2;
  }

  public int multiplyByTwoExceptionally(int number) {
    throw new RuntimeException("Exception thrown from MathService.multiplyByTwoExceptionally()");
  }

  public Map<Integer, Integer> multiplyListByTwo(List<Integer> numbers) {
    return StreamEx.of(numbers).toMap(i -> i, i -> i * 2);
  }

  public Map<Integer, Integer> multiplyListByTwoExceptionally(List<Integer> numbers) {
    throw new RuntimeException("Exception thrown from MathService.multiplyListByTwoExceptionally()");
  }

  public void echo(List<Integer> numbers) {
    System.out.println("echo: " + numbers);
  }
}
