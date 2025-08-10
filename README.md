# Expense Tracker

A modern JavaFX desktop application for tracking personal expenses with budget management, category organization, and detailed reporting capabilities.

## Features

### Core Functionality
- **Add, Edit, Delete Expenses**: Full CRUD operations for expense management
- **Category Management**: Create and manage expense categories with custom colors
- **Budget Tracking**: Set monthly budgets per category with real-time warnings
- **Monthly Reports**: Visual pie charts and detailed expense breakdowns
- **Search & Filter**: Find expenses by category, date, or notes
- **CSV Export**: Export monthly reports to CSV format

### User Interface
- **Modern Design**: Clean, intuitive interface with dark sidebar and light content area
- **Responsive Layout**: Split-pane design with pie chart and expense table
- **Color-coded Categories**: Visual category identification with custom colors
- **Budget Status Indicators**: Real-time budget warnings and status display
- **Month Navigation**: Easy navigation between different months

### Data Management
- **SQLite Database**: Lightweight, file-based database for data persistence
- **MVC Architecture**: Clean separation of concerns with Model-View-Controller pattern
- **Data Validation**: Comprehensive input validation and error handling
- **Offline Operation**: No internet connection required

## Technology Stack

- **Java 17**: Modern Java features and performance
- **JavaFX 17**: Rich desktop UI framework
- **SQLite**: Lightweight, embedded database
- **Maven**: Build automation and dependency management
- **JUnit 5**: Unit testing framework

## Project Structure

```
expense-tracker/
├── src/
│   ├── main/
│   │   ├── java/com/expensetracker/
│   │   │   ├── model/          # Data models
│   │   │   ├── dao/            # Data Access Objects
│   │   │   ├── service/        # Business logic
│   │   │   ├── ui/             # User interface controllers
│   │   │   └── util/           # Utility classes
│   │   └── resources/
│   │       ├── fxml/           # FXML layout files
│   │       └── css/            # Stylesheets
│   └── test/
│       └── java/com/expensetracker/
│           └── dao/            # Unit tests
├── pom.xml                     # Maven configuration
└── README.md                   # This file
```

## Database Schema

### Categories Table
```sql
CREATE TABLE categories (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT UNIQUE NOT NULL,
    color TEXT NOT NULL
);
```

### Expenses Table
```sql
CREATE TABLE expenses (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    amount REAL NOT NULL,
    category_id INTEGER NOT NULL,
    date TEXT NOT NULL,
    notes TEXT,
    FOREIGN KEY (category_id) REFERENCES categories (id)
);
```

### Budgets Table
```sql
CREATE TABLE budgets (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    category_id INTEGER NOT NULL,
    amount REAL NOT NULL,
    month INTEGER NOT NULL,
    year INTEGER NOT NULL,
    FOREIGN KEY (category_id) REFERENCES categories (id),
    UNIQUE(category_id, month, year)
);
```

## Prerequisites

- **Java 17** or higher
- **Maven 3.6** or higher
- **Git** (for cloning the repository)

## Installation & Setup

### 1. Clone the Repository
```bash
git clone <repository-url>
cd expense-tracker
```

### 2. Build the Project
```bash
mvn clean compile
```

### 3. Run the Application

#### Option A: Using Maven
```bash
mvn javafx:run
```

#### Option B: Using JAR File
```bash
mvn clean package
java -jar target/expense-tracker-1.0.0.jar
```

#### Option C: Using IDE
- Import the project into your IDE (IntelliJ IDEA, Eclipse, etc.)
- Run the `ExpenseTrackerApp` class

## Usage Guide

### Getting Started

1. **First Launch**: The application will automatically create the database and seed it with default categories
2. **Add Categories**: Use the "Manage > Categories" menu to create custom categories
3. **Set Budgets**: Use the "Manage > Budgets" menu to set monthly budgets for categories
4. **Add Expenses**: Click the "Add Expense" button to record new expenses

### Managing Expenses

#### Adding an Expense
1. Click the "Add Expense" button
2. Enter the amount
3. Select a category from the dropdown
4. Choose a date (defaults to today)
5. Add optional notes
6. Click "Save"

#### Editing an Expense
1. Find the expense in the table
2. Click the "Edit" button in the Actions column
3. Modify the details
4. Click "Save"

#### Deleting an Expense
1. Find the expense in the table
2. Click the "Delete" button in the Actions column
3. Confirm the deletion

### Budget Management

#### Setting a Budget
1. Go to "Manage > Budgets"
2. Select a category
3. Enter the monthly budget amount
4. Choose the month and year
5. Click "Save"

#### Budget Warnings
- **Green**: Budget is within limits
- **Yellow**: Budget is 90% or more used
- **Red**: Budget has been exceeded

### Reports and Export

#### Monthly View
- Use the month navigation arrows to view different months
- The pie chart shows expense distribution by category
- The table lists all expenses for the selected month

#### CSV Export
1. Navigate to the desired month
2. Go to "File > Export CSV"
3. Choose a save location
4. The exported file includes:
   - Summary statistics
   - Category breakdown
   - Detailed expense list

### Search and Filter

- Use the search field to filter expenses by:
  - Category name
  - Notes content
  - Amount values
- The search is case-insensitive and updates in real-time

## Development

### Running Tests
```bash
mvn test
```

### Code Style
The project follows standard Java conventions and includes:
- Comprehensive JavaDoc comments
- Consistent naming conventions
- Proper exception handling
- Logging throughout the application

### Adding New Features

#### Adding a New Model
1. Create the model class in the `model` package
2. Create corresponding DAO in the `dao` package
3. Create service layer in the `service` package
4. Add UI components as needed

#### Adding a New Dialog
1. Create FXML file in `resources/fxml/`
2. Create controller class in the `ui` package
3. Add dialog loading logic to main controller

### Database Migrations
The application uses SQLite with automatic schema creation. For future versions:
1. Add migration logic to `DatabaseManager`
2. Version the database schema
3. Include upgrade scripts

## Troubleshooting

### Common Issues

#### Application Won't Start
- Ensure Java 17+ is installed and in PATH
- Check that all dependencies are resolved
- Verify the database file has write permissions

#### Database Errors
- Delete the `expense_tracker.db` file to reset the database
- Check file permissions in the application directory

#### UI Issues
- Ensure JavaFX is properly installed
- Check that the CSS file is being loaded correctly

### Logging
The application uses Java's built-in logging. Logs are written to the console and can be configured in the application properties.

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Version History

### v1.0.0
- Initial release
- Basic expense tracking functionality
- Category and budget management
- Monthly reports and CSV export
- Modern JavaFX UI

## Support

For issues and questions:
1. Check the troubleshooting section
2. Review the code comments and documentation
3. Create an issue in the repository

## Acknowledgments

- JavaFX team for the excellent UI framework
- SQLite team for the lightweight database
- Maven team for the build system
- All contributors to the open-source libraries used
