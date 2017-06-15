package demo

import grails.persistence.Entity
import grails.testing.gorm.DataTest
import spock.lang.Ignore
import spock.lang.Specification

class UniqueConstraintOnHasOneSpec extends Specification implements DataTest {

    void setupSpec() {
        mockDomains Foo, Bar
    }

    void "Foo's name should be unique"() {
        given:
        def foo1 = new Foo(name: "FOO1")
        def bar = new Bar(name: "BAR1")
        foo1.bar = bar
        foo1.save()

        expect:
        Foo.count() == 1

        when:
        def foo2 = new Foo(name: "FOO1")
        foo2.bar = new Bar(name: "BAR2")
        foo2.save()

        then:
        foo2.hasErrors()
        foo2.errors['name']?.code == 'unique'
    }

    void "Foo's bar should be unique, but..."() {
        given:
        def foo1 = new Foo(name: "FOO1")
        def bar = new Bar(name: "BAR")
        foo1.bar = bar
        foo1.save(flush: true)

        expect:
        Foo.count() == 1
        Foo.findByBar(bar)

        when:
        def foo2 = new Foo(name: "FOO2")
        foo2.bar = bar // using same Bar instance
        foo2.save()

        then:
        foo2.hasErrors()
        foo2.errors['bar']?.code == 'unique'
    }
}

@Entity
class Bar {

    String name
    Foo foo

    static constraints = {
    }
}

@Entity
class Foo {

    String name
    static hasOne = [bar: Bar]

    static constraints = {
        name unique: true
        bar unique: true
    }
}