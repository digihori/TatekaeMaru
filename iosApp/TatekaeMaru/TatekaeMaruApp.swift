import SwiftUI
import TatekaeShared

struct SharedContentView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ controller: UIViewController, context: Context) {}
}

@main
struct TatekaeMaruApp: App {
    var body: some Scene {
        WindowGroup {
            SharedContentView().ignoresSafeArea(.keyboard)
        }
    }
}
