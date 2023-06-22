# EEP523-Final-Project
This is the final project for EE P 523: Android Application and Mobile Sensors

# Health Tracker

An Android application that keep records of health information including height, weight, step counts and heart rate.

## Introduction

This is a course project provided by UW ECE master's program (EE P 523).

This Android application has implemented the functionality of recording people's health condition and body measurements.

The heights and weights are manually input by users and a trending graph will be shown when users enter the detail page of each item.

Steps counting has used the inner sensor from Android, it detects the pattern of accelerator of the Android mobile phone and will stop counting at the end of the day. Similarly, the user can see the trending of steps counting day by day.

Heart rate detection is based on an algorithm based on the sensing of camera. It will need to ask for the permission of using camera and read data and learn the pattern of changing in brightness. When users put the finger onto the camera, the flashlight will be turned on, and the algorithm learns the pattern of brightness changing period. And conclude the heart rate from the pattern.

Similarly, all of four measurements contains a detail page allowing users to see measurement trending in different time scale, such as weekly, monthly and yearly.

## Features

* Record body measurements either by manual input or sensed by inner sensor
* Visualize data trending in graph view
* Detect heart rate in a high efficiency and low cost way, also in high accuracy

## Getting Started

### Prerequisites

Virtual testing environment: Pixel 3a Android 12
Physical testing environment: Redmi by Xiaomi

