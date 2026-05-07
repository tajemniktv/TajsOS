import UIKit
import SwiftUI
import ComposeApp

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        let database = DatabaseKt.createDatabase()
        let dataStore = DataStore_iosKt.createDataStore()
        let sharedModule = SharedModule(database: database, dataStore: dataStore)
        let viewModel = sharedModule.createViewModel(nextStepFallbackLabel: "Next step", untitledFallbackLabel: "Untitled")
        return MainViewControllerKt.MainViewController(viewModel: viewModel)
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    var body: some View {
        ComposeView()
            .ignoresSafeArea()
    }
}
