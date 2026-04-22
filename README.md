# Mahjong Game Application

A full-stack Mahjong game application featuring a Java Spring Boot backend and a React TypeScript frontend, containerized with Docker.

## Tech Stack

- **Backend**: Java 17, Spring Boot, Maven
- **Frontend**: TypeScript, React, Vite, Tailwind CSS
- **Database**: H2 (in-memory for development)
- **Containerization**: Docker & Docker Compose

## Quick Start

To run the entire application:

```bash
docker compose up --build
```

This will build and start both the backend and frontend services.

## Prerequisites

- Docker and Docker Compose installed on your system

## Architecture

The application consists of:

- **Backend** (`/backend`): REST API server handling game logic, AI opponents, and game state management
- **Frontend** (`/frontend`): React web application providing the user interface for playing Mahjong
- **Database**: H2 in-memory database for storing game states

## Services

- **Backend**: Runs on port 8080
- **Frontend**: Runs on port 80 (served by nginx)

## Development

### Backend Development

```bash
cd backend
./mvnw spring-boot:run
```

### Frontend Development

```bash
cd frontend
npm install
npm run dev
```

## API Documentation

The backend provides REST endpoints for:
- Creating and managing games
- Player actions (discard, chow, pong)
- AI opponent moves
- Game state retrieval

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test with `docker compose up --build`
5. Submit a pull request