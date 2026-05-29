#!/bin/bash

echo "======================================================"
echo "    TheKnife Restaurant Management System"
echo "======================================================"
echo

echo "Starting TheKnife application..."
java --module-path lib --add-modules javafx.controls,javafx.fxml --enable-native-access=javafx.graphics -jar bin/clientTK-client.jar

if [ $? -ne 0 ]; then
    echo
    echo "ERROR: Failed to start the application."
    echo "Make sure Java is installed."
    read -p "Press Enter to continue..."
fi
