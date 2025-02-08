Distributed Task Scheduler

Project Overview

The Distributed Task Scheduler is a Java-based application that schedules tasks across multiple worker nodes. It ensures efficient task execution, supports dependencies, and provides a dashboard for monitoring.

Features

Task scheduling across multiple workers

Fault tolerance for failed tasks

Task dependencies handling

Auto-scaling of worker nodes

Swing-based dashboard for monitoring

Technologies Used

Java (Swing, Multithreading)

MySQL (JDBC for database connectivity)

Setup & Installation

Set Up the Database (MySQL)

CREATE DATABASE task_scheduler;
USE task_scheduler;

CREATE TABLE tasks (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    status ENUM('PENDING', 'RUNNING', 'COMPLETED', 'FAILED') DEFAULT 'PENDING',
    worker_name VARCHAR(255),
    retry_count INT DEFAULT 0,
    dependency INT DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE worker_nodes (
    worker_name VARCHAR(255) PRIMARY KEY,
    status ENUM('ACTIVE', 'INACTIVE') DEFAULT 'ACTIVE'
);

Compile the Java Files

javac -cp "lib/*" -d out src/com/example/scheduler/*.java

Run the Application

java -cp "out;lib/*" com.example.scheduler.Main

How It Works

Tasks are assigned to available worker nodes.

Failed tasks are retried automatically.

Dependencies ensure tasks run in the correct order.

The Swing dashboard displays tasks and worker status.

Future Improvements

REST API for task management

Improved scheduling algorithms

