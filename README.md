[![Maven Central](https://maven-badges.herokuapp.com/maven-central/name.valery1707.junit/junit4-rule/badge.svg)](https://maven-badges.herokuapp.com/maven-central/name.valery1707.junit/junit4-rule)
[![License](https://img.shields.io/github/license/valery1707/junit4-rule.svg)](http://opensource.org/licenses/MIT)

[![codecov](https://codecov.io/gh/valery1707/junit4-rule/branch/master/graph/badge.svg)](https://codecov.io/gh/valery1707/junit4-rule)

[![Build Status](https://travis-ci.org/valery1707/junit4-rule.svg?branch=master)](https://travis-ci.org/valery1707/junit4-rule)
[![Build status](https://ci.appveyor.com/api/projects/status/lk3519k9dlw87kpx/branch/master?svg=true)](https://ci.appveyor.com/project/valery1707/junit4-rule/branch/master)
[![Sputnik](https://sputnik.ci/conf/badge)](https://sputnik.ci/app#/builds/valery1707/junit4-rule)

[![DevOps By Rultor.com](http://www.rultor.com/b/valery1707/junit4-rule)](http://www.rultor.com/p/valery1707/junit4-rule)

Library for JUnit 4 with some useful rules

# `ConditionalIgnoreRule`

Core mechanism for implementing custom conditions for ignore tests.

You can ignore test by custom rule only in 3 steps:
1. Create class implementing `name.valery1707.junit.rule.ConditionalIgnoreRule.IgnoreCondition`
1. Add `@org.junit.Rule`-annotated field in test class with `name.valery1707.junit.rule.ConditionalIgnoreRule`
1. Add annotation `name.valery1707.junit.rule.ConditionalIgnoreRule.ConditionalIgnore` with condition created at first step

Conditions can be reused by many test methods and ever test classes.
