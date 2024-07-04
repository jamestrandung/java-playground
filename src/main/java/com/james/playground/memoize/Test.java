package com.james.playground.memoize;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class Test {
  public static void main(String[] args) {
    System.out.println("STARTED SERIALIZATION");

    Functions.Func1<Integer, String> func1 = (String text) -> 1;
    Functions.Func1<Integer, String> func2 = String::length;

    try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("func1.ser"))) {
      out.writeObject(func1);

    } catch (IOException e) {
      System.out.println("Error: " + e.getMessage());
    }

    try (ObjectInputStream in = new ObjectInputStream(new FileInputStream("func1.ser"))) {
      Functions.Func1<Integer, String> deserializedFn = (Functions.Func1<Integer, String>) in.readObject();
      System.out.println("Length of 'Hello' is: " + deserializedFn.apply("Hello"));

    } catch (IOException | ClassNotFoundException e) {
      System.out.println("Error: " + e.getMessage());
    }

    try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("func2.ser"))) {
      out.writeObject(func2);

    } catch (IOException e) {
      System.out.println("Error: " + e.getMessage());
    }

    try (ObjectInputStream in = new ObjectInputStream(new FileInputStream("func2.ser"))) {
      Functions.Func1<Integer, String> deserializedFn = (Functions.Func1<Integer, String>) in.readObject();
      System.out.println("Length of 'Hello' is: " + deserializedFn.apply("Hello"));

    } catch (IOException | ClassNotFoundException e) {
      System.out.println("Error: " + e.getMessage());
    }

    System.out.println("COMPLETED SERIALIZATION");
  }
}
