# My Notes


ternary operator - `<if> ? <then> : <else>`

`@Override`  
`Object.equals(obj1, obj2)` and `Object.hash(args)`  
For nested array: `Arrays.deepEquals` and `Arrays.deepHashCode`  

declare an array - `public Type[] arr = new Type[Size];`  
initalize a nested array - `int[][] narr = {{1, 2}, {3, 4}};`  
`int[] copiedArr = Arrays.copyOf(arr, arr.length);`  
`Arrays.fill(arr, value); // int[] array`  

loops: `for (int i; i < 10; i++) {}`, `for(int value : array) {}`,   
`while () {}`, `do {} while ()`, `break`, `continue`  

Use stringbuilders for concatenation:   
`StringBuilder sb = new StringBuilder();`  
`sb.append("string")`  
`String str = sb.toString();`  

`String.format("string %s %d", "a string", 12)`  
- **`%s`** for strings.
- **`%d`** for decimal integers.
- **`%f`** for floating-point numbers.
- **`%b`** for booleans.
- **`%c`** for characters.

```
String result = switch (expression) {
    case 1 -> {
        doStuff;
        yield "hi";
    }
    default -> "Error";
}
```

or just to execute code:

```
switch (expression) {
    case var1 -> {};
    case var2 -> {};
};
```

