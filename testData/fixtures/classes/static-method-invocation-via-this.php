<?php

class MethodsHolder {
    public static function method() {}

    public static function staticContext() {
        // $this is not accessible in static context; Using $this when not in object context
        // $this->method();
    }

    public function nonStaticContext() {
        <warning descr="[EA] 'self::method(...)' should be used instead.">$this</warning>->method();

        static::method();
        self::method();
    }
}

function cases_holder() {
    $one = new MethodsHolder();
    <warning descr="[EA] '...::method(...)' should be used instead.">$one->method()</warning>;
    <warning descr="[EA] '...::method(...)' should be used instead.">(new MethodsHolder())->method()</warning>;
}

function parameters_and_used_variables_case_holder() {
    $use = new MethodsHolder();
    return function (MethodsHolder $parameter) use ($use) {
        return [$parameter->method(),  $use->method()];
    };
}
