#include <iostream>
#include <vector>
#include <unordered_map>

using namespace std;

int main() {
    // Fast I/O ke liye
    ios_base::sync_with_stdio(false);
    cin.tie(NULL);

    int n;
    if (!(cin >> n)) return 0;

    vector<int> nums(n);
    for (int i = 0; i < n; i++) {
        cin >> nums[i];
    }

    int target;
    cin >> target;

    // Hash map jo element aur uske index ko store karega
    unordered_map<int, int> numMap;

    for (int i = 0; i < n; i++) {
        int complement = target - nums[i];

        // Check karte hain ke complement map mein mojood hai ya nahi
        if (numMap.find(complement) != numMap.end()) {
            cout << numMap[complement] << " " << i << "\n";
            return 0;
        }

        // Current element aur uska index map mein dal dete hain
        numMap[nums[i]] = i;
    }

    return 0;
}