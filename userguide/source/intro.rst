.. _intro:


*******************
Preface & Overview
*******************

This guide is the official documentation for **DDOLib**, a Java library for
solving combinatorial optimisation problems using Decision Diagrams and
Dynamic Programming.


What is DDOLib?
===============

**DDOLib** is an open-source (MIT licence) Java solver for formulating and
solving dynamic-programming (DP) problems.
It was developed jointly by `UCLouvain <https://uclouvain.be/en/index.html>`_
(team of `Pierre Schaus <https://pschaus.github.io/>`_) and `CETIC <https://www.cetic.be/>`_
(team of `Renaud De Landtsheer <https://www.cetic.be/Renaud-DE-LANDTSHEER>`_).

DDOLib provides a clean modelling API: you describe your problem as a
labelled transition system (states, transitions, costs) and the library
takes care of building and searching decision diagrams for you.

Key features
------------

- **Multiple search strategies** – branch-and-bound with relaxed/restricted
  decision diagrams (DDO), A* search, Anytime Column Search (ACS), and exact
  MDD enumeration.
- **Dominance pruning** – discard states that can never improve on another.
- **Caching** – avoid revisiting the same sub-problems.
- **Relaxation framework** – build relaxed decision diagrams for strong
  lower bounds.
- **Rich example library** – Knapsack, TSP, TSPTW, Maximum Independent Set,
  Scheduling, and many more.


Theoretical Foundations
=======================

The technique of using decision diagrams for combinatorial optimisation was
introduced in:

    Bergman, D., Cire, A. A., Van Hoeve, W. J., & Hooker, J. N. (2016).
    *Discrete optimization with decision diagrams.*
    INFORMS Journal on Computing.

The library also implements ideas from the following papers:

* Alice Burlat, Roger Kameugne, Cristel Pelsser, Pierre Schaus (2026). *Clustering for Relaxed and Restricted Decision Diagram Bounds: When It Works and Why.*
  CPAIOR.
* Coppé, V., Gillard, X., & Schaus, P. (2024). *Decision diagram-based
  branch-and-bound with caching for dominance and suboptimality detection.*
  INFORMS Journal on Computing.
* Coppé, V., Gillard, X., & Schaus, P. (2024). *Modeling and Exploiting
  Dominance Rules for Discrete Optimization with Decision Diagrams.* CPAIOR.
* Gillard, X., & Schaus, P. (2022). *Large Neighborhood Search with
  Decision Diagrams.* IJCAI.
* Gillard, X., Schaus, P., & Coppé, V. (2021). *Ddo, a generic and
  efficient framework for MDD-based optimization.* IJCAI.

DDOLib is a Java port of the original `DDO <https://github.com/xgillard/ddo>`_
project implemented in Rust.


Javadoc
=======

The full `Javadoc API <https://ddolib-cetic-ucl.github.io/DDOLib/javadoc/index.html>`_.

.. _install:

Installation
============

Using DDOLib as a dependency
-----------------------------

DDOLib is published on
`Maven Central <https://central.sonatype.com/artifact/io.github.ddolib-cetic-ucl/ddolib>`_.
Add the following to your project's ``pom.xml``:

.. code-block:: xml

   <dependency>
       <groupId>io.github.ddolib-cetic-ucl</groupId>
       <artifactId>ddolib</artifactId>
       <version>0.0.5</version>
   </dependency>

Building from source
---------------------

DDOLib source code is hosted on `GitHub <https://github.com/DDOLIB-CETIC-UCL/DDOLib>`_.

**Using an IDE (IntelliJ IDEA recommended)**

.. code-block:: bash

   git clone https://github.com/DDOLIB-CETIC-UCL/DDOLib
   # Open IntelliJ → File → Open → select pom.xml → Open as Project

**From the command line (Maven)**

.. code-block:: bash

   mvn compile   # compile the project
   mvn test      # run all tests


Citing DDOLib
=============

If you use DDOLib in your research, please cite:

.. code-block:: bibtex

   @inproceedings{gillard2021ddo,
     title   = {Ddo, a generic and efficient framework for MDD-based optimization},
     author  = {Gillard, X. and Schaus, P. and Copp{\'e}, V.},
     booktitle = {IJCAI},
     year    = {2021}
   }


Acknowledgements
================

This project is funded by the Walloon Region (Belgium) as part of the
Win4Collective project (Convention 2410118).
